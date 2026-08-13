package io.github.kingarturito.projile.auth;

import io.github.kingarturito.projile.auth.security.UserPrincipal;
import io.github.kingarturito.projile.shared.exceptions.NotFoundException;
import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserPrincipalService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AuthenticatedUser authenticatedUser = userService.loadAuthenticatedUser(username);
            return new UserPrincipal(authenticatedUser);
        }catch (NotFoundException e){
            throw new UsernameNotFoundException(e.getMessage());
        }

    }
}
