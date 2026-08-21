package io.github.kingarturito.projile.user.dtos;
import java.util.UUID;

public record UserDto(
        UUID userId,
        String username,
        String email
) {}
