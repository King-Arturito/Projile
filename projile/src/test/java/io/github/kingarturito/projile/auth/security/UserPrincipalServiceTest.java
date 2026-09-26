package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.auth.UserPrincipalService;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.NotFoundException;
import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPrincipalServiceTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserPrincipalService userPrincipalService;

    @Test
    public void testFindUserByUsername() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "josh",
                "John@gmail.com",
                "$argon2idk"
        );
        when(userService.loadAuthenticatedUser(authenticatedUser.username())).thenReturn(authenticatedUser);
        UserDetails result = userPrincipalService.loadUserByUsername(authenticatedUser.username());

        assertEquals("josh", result.getUsername());
        assertEquals("$argon2idk", result.getPassword());
    }

    @Test
    void shouldThrowException() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "josh",
                "John@gmail.com",
                "$argon2idk"
        );
        when(userService.loadAuthenticatedUser(authenticatedUser.username()))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "Not found"));

        assertThrows(UsernameNotFoundException.class, ()  -> userPrincipalService.loadUserByUsername(authenticatedUser.username()));
    }
}
