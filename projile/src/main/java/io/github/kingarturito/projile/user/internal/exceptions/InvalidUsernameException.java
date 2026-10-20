package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;

public class InvalidUsernameException extends BadRequestException {

    public InvalidUsernameException() {
        super(ErrorCode.INVALID_USERNAME);
    }

    public InvalidUsernameException(String message) {
        super(ErrorCode.INVALID_USERNAME, message);
    }
}
