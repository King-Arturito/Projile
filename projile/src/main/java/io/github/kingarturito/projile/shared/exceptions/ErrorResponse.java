package io.github.kingarturito.projile.shared.exceptions;

import java.time.Instant;

/**
 * Uniform error payload returned by {@link GlobalExceptionHandler}.
 *
 * @param code      machine-readable failure identifier, see {@link ErrorCode}
 * @param message   human-readable detail of the failure
 * @param status    HTTP status code, mirrors the status of the response
 * @param timestamp moment the failure was reported
 */
public record ErrorResponse(String code, String message, int status, Instant timestamp) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.code(), message, errorCode.status().value(), Instant.now());
    }

    public static ErrorResponse of(ApiException exception) {
        return of(exception.getErrorCode(), exception.getMessage());
    }
}
