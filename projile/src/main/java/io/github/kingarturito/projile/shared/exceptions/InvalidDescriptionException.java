package io.github.kingarturito.projile.shared.exceptions;

public class InvalidDescriptionException extends BadRequestException {

    public InvalidDescriptionException() {
        super(ErrorCode.INVALID_DESCRIPTION);
    }

    public InvalidDescriptionException(String message) {
        super(ErrorCode.INVALID_DESCRIPTION, message);
    }
}
