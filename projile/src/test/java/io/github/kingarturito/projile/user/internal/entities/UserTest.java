package io.github.kingarturito.projile.user.internal.entities;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.user.internal.valueobjects.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserTest {
    private static final HashedPassword HASH = new HashedPassword("$argon2id$fixture");
    @Test void emailRegistrationStartsWithoutCredentialsOrNames() {
        User user = User.pendingEmail("Alex@EXAMPLE.com");
        assertThat(user.getUserEmail()).isEqualTo("alex@example.com");
        assertThat(user.getUsername()).isNull(); assertThat(user.getPassword()).isNull();
        assertThat(user.getDisplayName()).isNull(); assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isProfileComplete()).isFalse();
    }
    @Test void enforcesVerifiedEmailThenUsernameThenDisplayName() {
        User user = User.pendingEmail("alex@example.com");
        assertThatThrownBy(() -> user.chooseUsername(new Username("alex_rivera"))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> user.chooseDisplayName(new DisplayName("Alex Rivera"))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(user::verifyEmail).isInstanceOf(BadRequestException.class);
        user.completeEmailVerification(HASH);
        assertThat(user.isEmailVerified()).isTrue(); assertThat(user.isProfileComplete()).isFalse();
        assertThatThrownBy(() -> user.chooseDisplayName(new DisplayName("Alex Rivera"))).isInstanceOf(BadRequestException.class);
        user.chooseUsername(new Username("alex_rivera"));
        assertThat(user.isProfileComplete()).isFalse();
        user.chooseDisplayName(new DisplayName("Alex Rivera"));
        assertThat(user.isProfileComplete()).isTrue();
        assertThatThrownBy(() -> user.chooseUsername(new Username("another_name"))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> user.completeEmailVerification(HASH)).isInstanceOf(BadRequestException.class);
    }
    @Test void trustedOidcIdentityIsVerifiedButRequiresOnboarding() {
        User user = User.pendingOidc("alex@example.com", "https://accounts.google.com", "subject-1");
        assertThat(user.isEmailVerified()).isTrue(); assertThat(user.isProfileComplete()).isFalse();
        assertThat(user.getPassword()).isNull(); assertThat(user.getOidcSubject()).isEqualTo("subject-1");
    }
    @Test void oidcMayClaimOnlyACredentiallessPendingAccount() {
        User pending = User.pendingEmail("alex@example.com");
        pending.completeOidcRegistration("https://accounts.google.com", "subject");
        assertThat(pending.isEmailVerified()).isTrue();
        assertThatThrownBy(() -> pending.completeOidcRegistration("https://accounts.google.com", "other")).isInstanceOf(BadRequestException.class);
        User credentialBearing = User.pending("other@example.com", HASH);
        assertThatThrownBy(() -> credentialBearing.completeOidcRegistration("https://accounts.google.com", "subject")).isInstanceOf(BadRequestException.class);
    }
}
