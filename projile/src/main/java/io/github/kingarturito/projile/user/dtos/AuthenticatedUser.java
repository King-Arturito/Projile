package io.github.kingarturito.projile.user.dtos;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String username,
        String email,
        String hashedPassword,
        boolean emailVerified,
        boolean profileComplete
) {
    public AuthenticatedUser(UUID userId, String username, String email, String hashedPassword) {
        this(userId, username, email, hashedPassword, true, true);
    }
}
