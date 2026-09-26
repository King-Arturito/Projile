package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/** Failure caused by a clash with the current state of a resource; reported as {@code 409 Conflict}. */
public class ConflictException extends ApiException {

    public ConflictException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode.requireStatus(HttpStatus.CONFLICT), message);
    }
}
