package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;

public class InvalidPasswordException extends BadRequestException {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }

    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_PASSWORD, message);
    }
}
