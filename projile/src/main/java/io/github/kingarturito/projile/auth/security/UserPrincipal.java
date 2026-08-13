package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final AuthenticatedUser authenticatedUser;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return authenticatedUser.hashedPassword();
    }

    @Override
    public String getUsername() {
        return authenticatedUser.username();
    }

}
