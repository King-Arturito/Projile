package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Translates every {@link ApiException} into an {@link ErrorResponse}.
 *
 * <p>The HTTP status is taken from the {@link ErrorCode} carried by the exception, so adding a new
 * failure only requires a new code and exception, never a new handler method.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> handleInvalidRequest(Exception exception) {
        // Validation diagnostics can contain the submitted password/token; never echo rejected values.
        return ResponseEntity.badRequest().body(Map.of(
                "code", "INVALID_REQUEST", "message", "Check the submitted fields and try again."));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(ErrorResponse.of(e));
    }
}
