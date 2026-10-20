package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

import java.util.Locale;
import java.util.regex.Pattern;

/** The application's canonical email identity, shared by registration and identity-provider sign-in. */
public record Email(String value) {
    private static final Pattern SUPPORTED_ADDRESS = Pattern.compile(
            "^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+$");

    public Email {
        if (value == null) {
            throw new BadRequestException(ErrorCode.INVALID_EMAIL);
        }
        value = value.strip().toLowerCase(Locale.ROOT);
        int at = value.indexOf('@');
        if (value.length() > 254 || at < 1 || at > 64 || !SUPPORTED_ADDRESS.matcher(value).matches()
                || value.startsWith(".") || value.contains("..") || value.charAt(at - 1) == '.') {
            throw new BadRequestException(ErrorCode.INVALID_EMAIL);
        }
    }

    public static String normalize(String value) {
        return new Email(value).value();
    }

    @Override
    public String toString() {
        return value;
    }
}
