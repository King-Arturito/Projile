package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.UnauthorizedException;

public class InvalidPasswordException extends UnauthorizedException {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }

    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_PASSWORD, message);
    }
}
