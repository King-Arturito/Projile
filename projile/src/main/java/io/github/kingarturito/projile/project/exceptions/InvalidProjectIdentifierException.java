package io.github.kingarturito.projile.project.exceptions;

import io.github.kingarturito.projile.shared.BadRequestException;

public class InvalidProjectIdentifierException extends BadRequestException {
    public InvalidProjectIdentifierException(String message) {
        super(message);
    }
}
