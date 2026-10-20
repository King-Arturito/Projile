package io.github.kingarturito.projile.user;

public interface UserRegistrationService {
    UserId register(String email);
    void resendVerification(String email);
    UserId verifyEmail(String token, String password);
    UserId registerOidc(String email, String issuer, String subject);
}
