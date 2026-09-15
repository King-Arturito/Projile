package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import io.github.kingarturito.projile.user.dtos.UserDto;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.exceptions.UserNotFoundException;
import io.github.kingarturito.projile.user.internal.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto findUserByEmail(String email) {
        User user = userRepository.findByUserEmail(email).orElseThrow(
                () -> new UserNotFoundException("No user found with this email.")
        );
        return toDto(user);
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("No user found with this username."));
        return toDto(user);
    }

    @Override
    public AuthenticatedUser loadAuthenticatedUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("No user found with this username."));
        return toAuthRequest(user);
    }


    private UserDto toDto(User user) {
        return new UserDto(
                user.getUserId().userId(),
                user.getUsername().value(),
                user.getUserEmail()
        );
    }

    private AuthenticatedUser toAuthRequest(User user) {
        return new AuthenticatedUser(
                user.getUserId().userId(),
                user.getUsername().value(),
                user.getUserEmail(),
                user.getPassword().value()
        );
    }
}
