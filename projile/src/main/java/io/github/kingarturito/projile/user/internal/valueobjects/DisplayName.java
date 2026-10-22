package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidDisplayNameException;

/** A public, non-unique name. Ordinary interior spaces and Unicode letters are welcome. */
public record DisplayName(String value) {
    public DisplayName {
        if (value == null) {
            throw new InvalidDisplayNameException("Display name is required.");
        }
        if (value.codePoints().anyMatch(DisplayName::isUnsupportedCharacter)) {
            throw new InvalidDisplayNameException("Display name must not contain control or invisible formatting characters.");
        }
        value = value.strip();
        int length = value.codePointCount(0, value.length());
        if (value.isBlank() || length > 80) {
            throw new InvalidDisplayNameException("Display name must contain 1-80 characters.");
        }
    }

    private static boolean isUnsupportedCharacter(int codePoint) {
        int type = Character.getType(codePoint);
        return Character.isISOControl(codePoint)
                || type == Character.FORMAT
                || type == Character.SURROGATE
                || type == Character.LINE_SEPARATOR
                || type == Character.PARAGRAPH_SEPARATOR;
    }

    @Override
    public String toString() {
        return value;
    }
}
