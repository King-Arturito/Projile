package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidHashException extends BadRequestException {

    public InvalidHashException() {
        super(ErrorCode.INVALID_HASH);
    }

    public InvalidHashException(String message) {
        super(ErrorCode.INVALID_HASH, message);
    }
}
