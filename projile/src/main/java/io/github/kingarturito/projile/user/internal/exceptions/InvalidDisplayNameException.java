package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidDisplayNameException extends BadRequestException {
    public InvalidDisplayNameException(String message) {
        super(ErrorCode.INVALID_DISPLAY_NAME, message);
    }
}
