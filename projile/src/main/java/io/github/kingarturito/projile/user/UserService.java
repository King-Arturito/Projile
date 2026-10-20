package io.github.kingarturito.projile.user;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import io.github.kingarturito.projile.user.dtos.UserDto;

import java.util.UUID;

public interface UserService {
    UserDto findUserByEmail(String email);
    UserDto findByUsername(String username);
    AuthenticatedUser loadAuthenticatedUser(String username);
    AuthenticatedUser loadAuthenticatedUserById(UUID userId);
    UserDto findById(UUID userId);
    UserDto chooseUsername(UUID userId, String username);
    UserDto chooseDisplayName(UUID userId, String displayName);
}
