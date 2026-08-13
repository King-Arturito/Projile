package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidHashException;

public record HashedPassword(String value) {

    public HashedPassword {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("value cannot be null or empty");
        }
        if (!value.startsWith("$argon2")) {
            throw new InvalidHashException("Invalid Argon2 Hash");
        }
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}
