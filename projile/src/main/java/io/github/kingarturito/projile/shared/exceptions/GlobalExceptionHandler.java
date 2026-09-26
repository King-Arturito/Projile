package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates every {@link ApiException} into an {@link ErrorResponse}.
 *
 * <p>The HTTP status is taken from the {@link ErrorCode} carried by the exception, so adding a new
 * failure only requires a new code and exception, never a new handler method.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(ErrorResponse.of(e));
    }
}
