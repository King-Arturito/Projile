package io.github.kingarturito.projile.user.dtos;

import io.github.kingarturito.projile.user.internal.entities.User;

import java.util.UUID;

public record UserDto(
        UUID userId,
        String username,
        String email
) {}
