package io.github.kingarturito.projile.project.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidProjectIdentifierException extends BadRequestException {

    public InvalidProjectIdentifierException() {
        super(ErrorCode.INVALID_PROJECT_IDENTIFIER);
    }

    public InvalidProjectIdentifierException(String message) {
        super(ErrorCode.INVALID_PROJECT_IDENTIFIER, message);
    }
}
