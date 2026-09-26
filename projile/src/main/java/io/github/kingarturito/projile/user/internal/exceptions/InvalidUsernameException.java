package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.NotFoundException;

public class InvalidUsernameException extends NotFoundException {

    public InvalidUsernameException() {
        super(ErrorCode.INVALID_USERNAME);
    }

    public InvalidUsernameException(String message) {
        super(ErrorCode.INVALID_USERNAME, message);
    }
}
