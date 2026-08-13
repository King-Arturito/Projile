package io.github.kingarturito.projile.user;


public interface UserRegistrationService {
    UserId register(String email, String password);
}
