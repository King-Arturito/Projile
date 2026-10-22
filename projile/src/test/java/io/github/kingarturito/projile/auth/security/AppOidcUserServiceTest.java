package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AppOidcUserServiceTest {
    private final UserRegistrationService registration = mock(UserRegistrationService.class);
    private final UserService users = mock(UserService.class);
    private final AppOidcUserService service = new AppOidcUserService(registration, users);

    @Test void adaptsVerifiedProviderIdentityToLocalRolesWithoutUserInfoNetworkCalls() {
        UserId id = UserId.newId();
        when(registration.registerOidc("alex@example.com", "https://accounts.google.com", "subject")).thenReturn(id);
        when(users.loadAuthenticatedUserById(id.userId())).thenReturn(new AuthenticatedUser(id.userId(), null, "alex@example.com", null, true, false));
        var result = service.loadUser(request("google", "https://accounts.google.com", true, "alex@example.com"));
        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getName()).isEqualTo(id.userId().toString());
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_PROFILE_PENDING");
    }

    @ParameterizedTest @ValueSource(strings = {"registration", "issuer", "unverified", "missing-email"})
    void rejectsUntrustedOrIncompleteProviderClaims(String kind) {
        var request = request(kind.equals("registration") ? "other" : "google",
                kind.equals("issuer") ? "https://untrusted.example" : "https://accounts.google.com",
                !kind.equals("unverified"), kind.equals("missing-email") ? null : "alex@example.com");
        assertThatThrownBy(() -> service.loadUser(request)).isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(registration, users);
    }

    @Test void hidesAccountCollisionAndPersistenceErrors() {
        when(registration.registerOidc(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("private database diagnostic"));
        assertThatThrownBy(() -> service.loadUser(request("google", "https://accounts.google.com", true, "alex@example.com")))
                .isInstanceOf(OAuth2AuthenticationException.class).hasMessageNotContaining("private database");
    }

    private OidcUserRequest request(String registrationId, String issuer, boolean verified, String email) {
        var client = ClientRegistration.withRegistrationId(registrationId).clientId("test-client").clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://localhost/callback")
                .authorizationUri("https://provider.invalid/authorize").tokenUri("https://provider.invalid/token")
                .scope("openid", "email", "profile").build();
        Map<String,Object> claims = new HashMap<>(Map.of("sub", "subject", "iss", issuer, "aud", List.of("test-client"), "email_verified", verified));
        if (email != null) claims.put("email", email);
        Instant now = Instant.now();
        return new OidcUserRequest(client, new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test-access", now, now.plusSeconds(300)),
                new OidcIdToken("test-id-token", now, now.plusSeconds(300), claims));
    }
}
