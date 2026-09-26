package io.github.kingarturito.projile.shared.exceptions;

public class InvalidPositionException extends BadRequestException {

    public InvalidPositionException() {
        super(ErrorCode.INVALID_POSITION);
    }

    public InvalidPositionException(String message) {
        super(ErrorCode.INVALID_POSITION, message);
    }
}
