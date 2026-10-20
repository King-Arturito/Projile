package io.github.kingarturito.projile.user.dtos;
import java.util.UUID;

public record UserDto(
        UUID userId,
        String username,
        String email,
        String displayName,
        boolean emailVerified,
        boolean profileComplete
) {
    public UserDto(UUID userId, String username, String email) {
        this(userId, username, email, username, true, true);
    }
}
