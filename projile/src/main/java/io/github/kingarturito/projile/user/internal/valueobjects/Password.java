package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidPasswordException;

public record Password(String value) {

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 128;

    public Password {
        if (value == null) {
            throw new InvalidPasswordException("Password is required.");
        }
        validateLength(value);
        if (value.codePoints().anyMatch(Password::isUnsupportedCharacter)) {
            throw new InvalidPasswordException("Password must not contain control characters.");
        }
    }

    private static void validateLength(String value) {
        int length = value.codePointCount(0, value.length());
        if (length < MIN_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (length > MAX_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at most " + MAX_LENGTH + " characters long");
        }
    }

    private static boolean isUnsupportedCharacter(int codePoint) {
        int type = Character.getType(codePoint);
        return Character.isISOControl(codePoint)
                || type == Character.SURROGATE
                || type == Character.LINE_SEPARATOR
                || type == Character.PARAGRAPH_SEPARATOR;
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}
