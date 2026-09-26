package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/** Failure caused by a missing resource; reported as {@code 404 Not Found}. */
public class NotFoundException extends ApiException {

    public NotFoundException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode.requireStatus(HttpStatus.NOT_FOUND), message);
    }
}
