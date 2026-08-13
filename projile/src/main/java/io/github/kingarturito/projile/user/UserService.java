package io.github.kingarturito.projile.user;

import io.github.kingarturito.projile.user.dtos.AuthenticatedUser;
import io.github.kingarturito.projile.user.dtos.UserDto;

public interface UserService {
    UserDto findUserByEmail(String email);
    UserDto findByUsername(String username);
    AuthenticatedUser loadAuthenticatedUser(String username);
}
