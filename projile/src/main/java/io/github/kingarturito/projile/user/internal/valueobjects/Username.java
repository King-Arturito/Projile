package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidUsernameException;

import java.util.regex.Pattern;

public record Username(String value) {
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 32;

    // letters, digits, underscores only; must start with a letter
    private static final Pattern VALID_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    public Username {
        validate(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUsernameException("Username must not be null or blank");
        }

        int length = value.length();

        if (length < MIN_LENGTH) {
            throw new InvalidUsernameException(
                    "Username must be at least " + MIN_LENGTH + " characters long, but was " + length
            );
        }

        if (length > MAX_LENGTH) {
            throw new InvalidUsernameException(
                    "Username must be at most " + MAX_LENGTH + " characters long, but was " + length
            );
        }

        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new InvalidUsernameException(
                    "Username must start with a letter and contain only letters, digits, or underscores"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}