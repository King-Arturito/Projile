package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.shared.exceptions.ConflictException;
import io.github.kingarturito.projile.support.AuthIntegrationSupport;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.dtos.UserDto;
import io.github.kingarturito.projile.user.internal.exceptions.EmailAlreadyRegisteredException;
import io.github.kingarturito.projile.user.internal.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserRegistrationIT extends AuthIntegrationSupport {

    private static final String INVALID_LINK_MESSAGE = "The verification link is invalid or has expired.";

    // ---------- helpers ----------

    private String sha256Hex(String raw) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private String storedDigest(UserId id) {
        return jdbc.queryForObject("SELECT token_hash FROM email_verifications WHERE user_id=?", String.class, id.userId());
    }

    private long tokenCount() {
        return jdbc.queryForObject("SELECT count(*) FROM email_verifications", Long.class);
    }

    private String storedPasswordHash(UserId id) {
        return repository.findById(id).orElseThrow().getPassword().value();
    }

    private boolean isVerified(UserId id) {
        return users.findById(id.userId()).emailVerified();
    }

    private void assertTokenRejected(String token) {
        assertThatThrownBy(() -> registration.verifyEmail(token, PASSWORD))
                .isInstanceOf(ResponseStatusException.class);
    }

    private void failMailTransport() {
        doThrow(new MailSendException("simulated offline mail transport"))
                .when(mail).send(any(SimpleMailMessage.class));
    }

    /** Returns the result, or the exception if the operation threw. */
    private Object outcome(Supplier<?> operation) {
        try {
            return operation.get();
        } catch (RuntimeException failure) {
            return failure;
        }
    }

    /** Runs both operations at the same moment on separate threads and returns both outcomes. */
    private List<Object> race(Supplier<?> first, Supplier<?> second) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Supplier<?> task : List.of(first, second)) {
                futures.add(workers.submit(() -> {
                    ready.countDown();
                    start.await();
                    return outcome(task);
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(
                    futures.get(0).get(15, TimeUnit.SECONDS),
                    futures.get(1).get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    // ---------- tokens and registration ----------

    @Test
    void deliversRandomTokenButStoresOnlyDigestAndThirtyMinuteExpiry() throws Exception {
        Instant before = Instant.now();
        UserId id = pending("Alex@EXAMPLE.com");
        String raw = emailedToken();

        assertThat(raw).matches("[A-Za-z0-9_-]{43}");
        assertThat(storedDigest(id)).isEqualTo(sha256Hex(raw)).isNotEqualTo(raw);

        ArgumentCaptor<SimpleMailMessage> delivered = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(delivered.capture());
        assertThat(delivered.getValue().getTo()).containsExactly("alex@example.com");
        assertThat(delivered.getValue().getText()).doesNotContain(PASSWORD, "$argon2");

        Instant expires = jdbc.queryForObject(
                "SELECT expires_at FROM email_verifications WHERE user_id=?",
                (rs, n) -> rs.getTimestamp(1).toInstant(),
                id.userId());
        assertThat(expires).isBetween(before.plusSeconds(1799), Instant.now().plusSeconds(1801));

        var user = users.findById(id.userId());
        assertThat(user.emailVerified()).isFalse();
        assertThat(user.username()).isNull();
        assertThat(user.displayName()).isNull();
        assertThat(repository.findById(id).orElseThrow().getPassword()).isNull();

        UserId other = pending("other@example.com");
        assertThat(storedDigest(other)).isNotEqualTo(storedDigest(id));
    }

    @Test
    void registrationAndResendRespectCooldownAndNeverReplaceVerifiedCredentials() throws Exception {
        UserId id = pending("repeat@example.com");
        String raw = emailedToken();
        String originalDigest = storedDigest(id);

        assertThat(pending("REPEAT@example.com")).isEqualTo(id);
        registration.resendVerification("repeat@example.com");
        verify(mail, times(1)).send(any(SimpleMailMessage.class));
        assertThat(storedDigest(id)).isEqualTo(originalDigest);

        registration.verifyEmail(raw, PASSWORD);
        String passwordHash = storedPasswordHash(id);

        registration.register("repeat@example.com");
        registration.resendVerification("repeat@example.com");
        assertThat(storedPasswordHash(id)).isEqualTo(passwordHash);
        assertThat(tokenCount()).isZero();
        verify(mail, times(1)).send(any(SimpleMailMessage.class));

        registration.resendVerification("unknown@example.com");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void resendingAfterCooldownInvalidatesPreviousToken() {
        UserId id = pending("rotate@example.com");
        String previous = emailedToken();
        ageToken(id, 61);

        registration.resendVerification("rotate@example.com");
        String fresh = emailedToken();

        assertThat(fresh).isNotEqualTo(previous);
        assertTokenRejected(previous);
        registration.verifyEmail(fresh, PASSWORD);
        assertThat(tokenCount()).isZero();
        assertTokenRejected(fresh);
    }

    @Test
    void unknownMalformedAndExpiredTokensHaveTheSameFailureAndLeaveAccountUnverified() {
        UserId id = pending("expired@example.com");
        String expired = emailedToken();
        jdbc.update("UPDATE email_verifications SET expires_at=now()-interval '1 second' WHERE user_id=?", id.userId());

        for (String token : List.of(expired, "A".repeat(43), "malformed")) {
            assertThatThrownBy(() -> registration.verifyEmail(token, PASSWORD))
                    .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                        assertThat(e.getStatusCode().value()).isEqualTo(400);
                        assertThat(e.getReason()).isEqualTo(INVALID_LINK_MESSAGE);
                    });
        }

        assertThat(isVerified(id)).isFalse();
        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    void weakPasswordDoesNotConsumeVerificationAndAValidPasswordCompletesItAtomically() {
        UserId id = pending("weak@example.com");
        String raw = emailedToken();

        assertThatThrownBy(() -> registration.verifyEmail(raw, "short"))
                .isInstanceOf(InvalidPasswordException.class);
        assertThat(isVerified(id)).isFalse();
        assertThat(tokenCount()).isEqualTo(1);

        assertThat(registration.verifyEmail(raw, PASSWORD)).isEqualTo(id);

        var stored = repository.findById(id).orElseThrow();
        assertThat(stored.isEmailVerified()).isTrue();
        assertThat(encoder.matches(PASSWORD, stored.getPassword().value())).isTrue();
        assertThat(stored.getPassword().value()).doesNotContain(PASSWORD);
        assertThat(tokenCount()).isZero();
    }

    @Test
    void smtpFailureRollsBackNewRegistrationAndPreservesPreviousTokenOnRenewal() {
        failMailTransport();
        assertThatThrownBy(() -> pending("offline@example.com"))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(repository.count()).isZero();
        assertThat(tokenCount()).isZero();

        reset(mail);
        UserId id = pending("renew@example.com");
        String previous = emailedToken();
        String originalDigest = storedDigest(id);
        ageToken(id, 61);

        failMailTransport();
        assertThatThrownBy(() -> registration.resendVerification("renew@example.com"))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(storedDigest(id)).isEqualTo(originalDigest);

        registration.verifyEmail(previous, PASSWORD);
        assertThat(isVerified(id)).isTrue();
    }

    @Test
    void mailboxProofLetsAnExistingUnverifiedAccountEstablishANewPassword() {
        UserId id = credentialBearingUnverified("legacy@example.com");
        registration.resendVerification("legacy@example.com");

        String replacement = "a different private passphrase";
        registration.verifyEmail(emailedToken(), replacement);

        String storedHash = storedPasswordHash(id);
        assertThat(encoder.matches(replacement, storedHash)).isTrue();
        assertThat(encoder.matches(PASSWORD, storedHash)).isFalse();
    }

    // ---------- OIDC ----------

    @Test
    void oidcIdentityUsesIssuerAndSubjectAndNeverLinksPasswordAccountsByEmail() {
        UserId id = registration.registerOidc("google@example.com", ISSUER, "subject");
        assertThat(registration.registerOidc("changed@example.com", ISSUER, "subject")).isEqualTo(id);

        var user = users.findById(id.userId());
        assertThat(repository.count()).isEqualTo(1);
        assertThat(user.email()).isEqualTo("google@example.com");
        assertThat(user.emailVerified()).isTrue();
        assertThat(user.profileComplete()).isFalse();

        credentialBearingUnverified("password@example.com");
        assertThatThrownBy(() -> registration.registerOidc("password@example.com", ISSUER, "different-subject"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void verifiedGoogleIdentityCanClaimAPendingEmailAndInvalidatesItsLink() {
        UserId pendingId = pending("switch@example.com");
        String token = emailedToken();

        assertThat(registration.registerOidc("switch@example.com", ISSUER, "subject")).isEqualTo(pendingId);

        assertThat(tokenCount()).isZero();
        assertThat(repository.findById(pendingId).orElseThrow().getPassword()).isNull();
        assertTokenRejected(token);
    }

    // ---------- concurrency ----------

    @Test
    void concurrentRegistrationsPreserveOneNormalizedIdentity() throws Exception {
        List<Object> result = race(
                () -> pending("race@example.com"),
                () -> pending("RACE@example.com"));

        assertThat(repository.count()).isEqualTo(1);
        assertThat(tokenCount()).isEqualTo(1);
        assertThat(result).anyMatch(UserId.class::isInstance);
        assertThat(result).allMatch(v -> v instanceof UserId || v instanceof DataIntegrityViolationException);
    }

    @Test
    void concurrentVerificationCanConsumeOneTokenOnlyOnce() throws Exception {
        UserId id = pending("consume@example.com");
        String token = emailedToken();

        List<Object> result = race(
                () -> registration.verifyEmail(token, PASSWORD),
                () -> registration.verifyEmail(token, PASSWORD));

        assertThat(result.stream().filter(UserId.class::isInstance)).hasSize(1);
        assertThat(result.stream().filter(ResponseStatusException.class::isInstance)).hasSize(1);
        assertThat(isVerified(id)).isTrue();
        assertThat(tokenCount()).isZero();
    }

    @Test
    void concurrentResendAndVerificationCannotResurrectSupersededTokensOrDeadlock() throws Exception {
        UserId id = pending("resend-race@example.com");
        String old = emailedToken();
        ageToken(id, 61);

        List<Object> result = race(
                () -> {
                    registration.resendVerification("resend-race@example.com");
                    return "resent";
                },
                () -> registration.verifyEmail(old, PASSWORD));

        assertThat(result).allMatch(v -> v instanceof String || v instanceof UserId || v instanceof ResponseStatusException);

        if (isVerified(id)) {
            assertThat(tokenCount()).isZero();
        } else {
            assertTokenRejected(old);
            registration.verifyEmail(emailedToken(), PASSWORD);
            assertThat(tokenCount()).isZero();
        }
    }

    @Test
    void concurrentCaseOnlyUsernameClaimsHaveOneWinnerAndOneConflict() throws Exception {
        UserId first = verified("first@example.com");
        UserId second = verified("second@example.com");

        List<Object> result = race(
                () -> users.chooseUsername(first.userId(), "Reserved_User"),
                () -> users.chooseUsername(second.userId(), "reserved_user"));

        assertThat(result.stream().filter(UserDto.class::isInstance)).hasSize(1);
        assertThat(result.stream().filter(ConflictException.class::isInstance)).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE lower(username)='reserved_user'", Long.class))
                .isEqualTo(1);
    }

    @Test
    void concurrentOidcFirstSignInsDoNotDuplicateProviderIdentity() throws Exception {
        List<Object> result = race(
                () -> registration.registerOidc("oidc-race@example.com", ISSUER, "race-sub"),
                () -> registration.registerOidc("oidc-race@example.com", ISSUER, "race-sub"));

        assertThat(result).anyMatch(UserId.class::isInstance);
        assertThat(result).allMatch(v -> v instanceof UserId
                || v instanceof DataIntegrityViolationException
                || v instanceof EmailAlreadyRegisteredException);
        assertThat(repository.count()).isEqualTo(1);
    }

    // ---------- database constraints ----------

    @Test
    void postgresEnforcesCaseInsensitiveAndIdentityConstraintsAndCascadesTokens() {
        UserId id = pending("constraints@example.com");

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO users(user_id,user_email) VALUES (?,?)",
                UUID.randomUUID(), "CONSTRAINTS@example.com"))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO users(user_id,user_email,email_verified) VALUES (?,?,true)",
                UUID.randomUUID(), "no-method@example.com"))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO users(user_id,user_email,oidc_issuer) VALUES (?,?,?)",
                UUID.randomUUID(), "half-identity@example.com", ISSUER))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("DELETE FROM users WHERE user_id=?", id.userId());
        assertThat(tokenCount()).isZero();
    }
}