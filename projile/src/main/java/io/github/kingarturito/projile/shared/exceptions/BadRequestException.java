package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/** Failure caused by an invalid request payload; reported as {@code 400 Bad Request}. */
public class BadRequestException extends ApiException {

    public BadRequestException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode.requireStatus(HttpStatus.BAD_REQUEST), message);
    }
}
