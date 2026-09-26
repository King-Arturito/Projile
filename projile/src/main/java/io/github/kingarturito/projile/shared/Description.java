package io.github.kingarturito.projile.shared;

import io.github.kingarturito.projile.shared.exceptions.InvalidDescriptionException;

public record Description(String value) {

    public static final int MAX_LENGTH = 2000;

    public Description {
        if (value == null) {
            throw new InvalidDescriptionException("Description must not be null");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new InvalidDescriptionException(
                    "Description must not exceed " + MAX_LENGTH + " characters (was " + value.length() + ")"
            );
        }
    }
}