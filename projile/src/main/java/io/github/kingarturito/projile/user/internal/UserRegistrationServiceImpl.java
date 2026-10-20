package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import io.github.kingarturito.projile.user.internal.entities.EmailVerification;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.exceptions.EmailAlreadyRegisteredException;
import io.github.kingarturito.projile.user.internal.repositories.EmailVerificationRepository;
import io.github.kingarturito.projile.user.internal.repositories.UserRepository;
import io.github.kingarturito.projile.user.internal.valueobjects.Email;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import io.github.kingarturito.projile.user.internal.valueobjects.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationServiceImpl implements UserRegistrationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationMailSender mailSender;

    @Override
    public UserId register(String email) {
        String normalizedEmail = Email.normalize(email);
        UserId existingId = userRepository.findUserIdByEmail(normalizedEmail).orElse(null);
        User user = existingId == null
                ? userRepository.saveAndFlush(User.pendingEmail(normalizedEmail))
                : userRepository.lockById(existingId).orElseThrow();
        // Repeated registration never creates a login session or alters a verified account.
        if (!user.isEmailVerified()) sendVerification(user);
        return user.getUserId();
    }

    @Override
    public void resendVerification(String email) {
        UserId existingId = userRepository.findUserIdByEmail(Email.normalize(email)).orElse(null);
        if (existingId == null) return;
        User user = userRepository.lockById(existingId).orElseThrow();
        if (!user.isEmailVerified()) sendVerification(user);
    }

    @Override
    public UserId verifyEmail(String token, String password) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}") || password == null || password.length() > 256) {
            throw invalidVerification();
        }
        // A scalar lookup avoids caching token state before a concurrent resend releases the user lock.
        UUID userId = verificationRepository.findUserIdByTokenHash(digest(token)).orElseThrow(
                UserRegistrationServiceImpl::invalidVerification);
        // Take locks in user-then-token order, the same order used by resends.
        User user = userRepository.lockById(new UserId(userId)).orElseThrow(
                UserRegistrationServiceImpl::invalidVerification);
        EmailVerification verification = verificationRepository.lockByUserId(userId).orElseThrow(
                UserRegistrationServiceImpl::invalidVerification);
        if (!verification.getTokenHash().equals(digest(token)) || !verification.getExpiresAt().isAfter(Instant.now())
                || user.isEmailVerified()) {
            throw invalidVerification();
        }
        Password verifiedPassword = new Password(password);
        user.completeEmailVerification(new HashedPassword(passwordEncoder.encode(verifiedPassword.value())));
        userRepository.saveAndFlush(user);
        verificationRepository.delete(verification);
        verificationRepository.flush();
        return user.getUserId();
    }

    @Override
    public UserId registerOidc(String email, String issuer, String subject) {
        User knownIdentity = userRepository.findByOidcIssuerAndOidcSubject(issuer, subject).orElse(null);
        if (knownIdentity != null) return knownIdentity.getUserId();
        String normalizedEmail = Email.normalize(email);
        UserId existingId = userRepository.findUserIdByEmail(normalizedEmail).orElse(null);
        if (existingId != null) {
            User pending = userRepository.lockById(existingId).orElseThrow();
            // A signup email alone must not block its owner from choosing Google instead.
            if (!pending.isEmailVerified() && pending.getPassword() == null && pending.getUsername() == null
                    && pending.getOidcSubject() == null) {
                pending.completeOidcRegistration(issuer, subject);
                verificationRepository.lockByUserId(existingId.userId()).ifPresent(verificationRepository::delete);
                userRepository.saveAndFlush(pending);
                return pending.getUserId();
            }
            // Verified and credential-bearing accounts require explicit account linking, a separate feature.
            throw new EmailAlreadyRegisteredException("Sign in using the original account method.");
        }
        return userRepository.saveAndFlush(User.pendingOidc(normalizedEmail, issuer, subject)).getUserId();
    }

    private void sendVerification(User user) {
        Instant now = Instant.now();
        EmailVerification verification = verificationRepository.lockByUserId(user.getUserId().userId()).orElse(null);
        // Silent cooldown preserves the same HTTP response for every email address.
        if (verification != null && verification.getSentAt().isAfter(now.minusSeconds(60))) return;
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        if (verification == null) {
            verification = new EmailVerification(user.getUserId().userId(), digest(token), now);
        } else {
            verification.renew(digest(token), now);
        }
        verificationRepository.saveAndFlush(verification);
        // A delivery failure rolls back the token change and new registration.
        mailSender.sendVerification(user.getUserEmail(), token);
    }

    private static ResponseStatusException invalidVerification() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "The verification link is invalid or has expired.");
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
