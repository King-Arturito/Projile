package io.github.kingarturito.projile.user.dtos;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String username,
        String email,
        String hashedPassword
){}
