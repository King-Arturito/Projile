package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class of every exception the API reports to clients.
 *
 * <p>An exception always carries an {@link ErrorCode}, which identifies the failure and determines
 * the HTTP status, plus an optional message that adds request-specific detail. When no message is
 * given the default message of the code is used.</p>
 */
public abstract class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    protected ApiException(ErrorCode errorCode, String message) {
        super(message == null || message.isBlank() ? errorCode.defaultMessage() : message);
        this.errorCode = errorCode;
    }

    /** The machine-readable identifier of this failure. */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** HTTP status this failure must be reported with. */
    public HttpStatus getStatus() {
        return errorCode.status();
    }
}
