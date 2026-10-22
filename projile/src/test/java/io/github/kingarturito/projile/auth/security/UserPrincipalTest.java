package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class UserPrincipalTest {
    @Test void unverifiedAccountsAreDisabledWithoutAuthorities() {
        UserPrincipal principal = principal(false, false, null);
        assertThat(principal.isEnabled()).isFalse(); assertThat(principal.getAuthorities()).isEmpty();
    }
    @Test void verifiedIncompleteAccountsHaveOnlyOnboardingAuthorityAndEmailFallback() {
        UserPrincipal principal = principal(true, false, null);
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_PROFILE_PENDING");
        assertThat(principal.getUsername()).isEqualTo("alex@example.com");
    }
    @Test void completeAccountsHaveUserAuthority() {
        assertThat(principal(true, true, "alex_rivera").getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }
    private UserPrincipal principal(boolean verified, boolean complete, String username) {
        return new UserPrincipal(new AuthenticatedUser(UUID.randomUUID(), username, "alex@example.com", null, verified, complete));
    }
}
