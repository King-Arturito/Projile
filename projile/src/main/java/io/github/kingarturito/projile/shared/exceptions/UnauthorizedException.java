package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/** Failure caused by missing or rejected credentials; reported as {@code 401 Unauthorized}. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode.requireStatus(HttpStatus.UNAUTHORIZED), message);
    }
}
