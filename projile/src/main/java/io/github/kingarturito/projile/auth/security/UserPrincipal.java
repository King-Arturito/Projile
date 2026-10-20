package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {
    private final AuthenticatedUser authenticatedUser;

    public UserPrincipal(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public UUID getUserId() {
        return authenticatedUser.userId();
    }

    public boolean isProfileComplete() {
        return authenticatedUser.profileComplete();
    }

    public boolean hasUsername() {
        return authenticatedUser.username() != null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!authenticatedUser.emailVerified()) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority(
                authenticatedUser.profileComplete() ? "ROLE_USER" : "ROLE_PROFILE_PENDING"));
    }

    @Override
    public String getPassword() {
        return authenticatedUser.hashedPassword();
    }

    @Override
    public String getUsername() {
        return authenticatedUser.username() == null ? authenticatedUser.email() : authenticatedUser.username();
    }

    @Override
    public boolean isEnabled() {
        return authenticatedUser.emailVerified();
    }
}
