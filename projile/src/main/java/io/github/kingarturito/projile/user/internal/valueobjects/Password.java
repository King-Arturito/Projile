package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidPasswordException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Password(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^A-Za-z0-9]");

    public Password {
        Objects.requireNonNull(value, "password must not be null");
        validateLength(value);
        validateComplexity(value);
    }

    private static void validateLength(String value) {
        if (value.length() < MIN_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at most " + MAX_LENGTH + " characters long");
        }
    }

    private static void validateComplexity(String value) {
        if (!UPPERCASE.matcher(value).find()) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }
        if (!LOWERCASE.matcher(value).find()) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }
        if (!DIGIT.matcher(value).find()) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }
        if (!SPECIAL_CHAR.matcher(value).find()) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}