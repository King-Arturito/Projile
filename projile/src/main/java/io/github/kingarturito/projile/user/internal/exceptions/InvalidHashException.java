package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;

public class InvalidHashException extends BadRequestException {
    public InvalidHashException(String message) {
        super(message);
    }
}
