package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import io.github.kingarturito.projile.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final UserRegistrationService registrationService;
    private final UserService userService;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser providerUser = delegate.loadUser(request);
        String issuer = providerUser.getIdToken().getIssuer().toExternalForm();
        String email = providerUser.getIdToken().getEmail();
        if (!"google".equals(request.getClientRegistration().getRegistrationId())
                || !"https://accounts.google.com".equals(issuer)
                || !Boolean.TRUE.equals(providerUser.getIdToken().getEmailVerified())
                || email == null || email.isBlank()) {
            throw rejectedIdentity();
        }

        try {
            // Persistence resolves issuer + subject. Matching emails never link unrelated accounts.
            UserId userId = registrationService.registerOidc(email, issuer, providerUser.getSubject());
            return new OidcUserPrincipal(userService.loadAuthenticatedUserById(userId.userId()), providerUser);
        } catch (RuntimeException exception) {
            throw rejectedIdentity();
        }
    }

    private OAuth2AuthenticationException rejectedIdentity() {
        return new OAuth2AuthenticationException(new OAuth2Error("access_denied"), "Unable to sign in with Google");
    }
}
