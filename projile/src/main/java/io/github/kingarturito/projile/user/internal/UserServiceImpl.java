package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.shared.exceptions.ConflictException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import io.github.kingarturito.projile.user.dtos.UserDto;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.exceptions.UserNotFoundException;
import io.github.kingarturito.projile.user.internal.repositories.UserRepository;
import io.github.kingarturito.projile.user.internal.valueobjects.DisplayName;
import io.github.kingarturito.projile.user.internal.valueobjects.Email;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto findUserByEmail(String email) {
        User user = userRepository.findByUserEmail(Email.normalize(email)).orElseThrow(
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

    @Override
    public AuthenticatedUser loadAuthenticatedUserById(UUID userId) {
        return toAuthRequest(requiredUser(userId));
    }

    @Override
    public UserDto findById(UUID userId) {
        return toDto(requiredUser(userId));
    }

    @Override
    @Transactional
    public UserDto chooseUsername(UUID userId, String username) {
        Username validatedUsername = new Username(username);
        User user = lockedUser(userId);
        if (user.getUsername() == null && userRepository.existsByUsername(validatedUsername)) {
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_REGISTERED);
        }
        user.chooseUsername(validatedUsername);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // A second registration can win between the availability check and the write.
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_REGISTERED);
        }
        return toDto(user);
    }

    @Override
    @Transactional
    public UserDto chooseDisplayName(UUID userId, String displayName) {
        User user = lockedUser(userId);
        user.chooseDisplayName(new DisplayName(displayName));
        return toDto(userRepository.saveAndFlush(user));
    }

    private User requiredUser(UUID userId) {
        return userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private User lockedUser(UUID userId) {
        return userRepository.lockById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getUserId().userId(),
                user.getUsername() == null ? null : user.getUsername().value(),
                user.getUserEmail(),
                user.getDisplayName() == null ? null : user.getDisplayName().value(),
                user.isEmailVerified(),
                user.isProfileComplete()
        );
    }

    private AuthenticatedUser toAuthRequest(User user) {
        return new AuthenticatedUser(
                user.getUserId().userId(),
                user.getUsername() == null ? null : user.getUsername().value(),
                user.getUserEmail(),
                user.getPassword() == null ? null : user.getPassword().value(),
                user.isEmailVerified(),
                user.isProfileComplete()
        );
    }
}
