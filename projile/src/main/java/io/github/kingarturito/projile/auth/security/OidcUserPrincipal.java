package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;

/** Keeps the provider identity and local authorization together in the server session. */
final class OidcUserPrincipal extends UserPrincipal implements OidcUser {
    private final OidcUser providerUser;

    OidcUserPrincipal(AuthenticatedUser localUser, OidcUser providerUser) {
        super(localUser);
        this.providerUser = providerUser;
    }

    @Override
    public Map<String, Object> getClaims() {
        return providerUser.getClaims();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return providerUser.getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return providerUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return providerUser.getIdToken();
    }

    @Override
    public String getName() {
        return getUserId().toString();
    }
}
