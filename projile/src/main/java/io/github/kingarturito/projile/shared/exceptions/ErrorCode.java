package io.github.kingarturito.projile.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Stable, machine-readable identifiers for every error the API can return.
 *
 * <p>Each {@link ApiException} carries one of these codes so clients can react to a specific
 * failure without parsing a human-readable message. The declared {@link HttpStatus} is the status
 * {@link GlobalExceptionHandler} responds with; it is validated against the exception type the code
 * is used with, see {@link #requireStatus(HttpStatus)}.</p>
 *
 * <p>Enum names are part of the public API contract: rename with care and never reuse a code for a
 * different failure.</p>
 */
public enum ErrorCode {

    // 400 Bad Request - the request is well-formed but the payload is not acceptable.
    INVALID_POSITION(HttpStatus.BAD_REQUEST,
            "Position must be a 1-64 character fractional index (base-62 with '|' and ':')."),
    INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST,
            "Description must not be null and must not exceed 2000 characters."),
    INVALID_BOARD_NAME(HttpStatus.BAD_REQUEST,
            "Board name is required and must not exceed 120 characters."),
    INVALID_COLUMN_NAME(HttpStatus.BAD_REQUEST,
            "Column name is required and must not exceed 120 characters."),
    INVALID_TASK_TITLE(HttpStatus.BAD_REQUEST,
            "Task title is required and must not exceed the allowed length."),
    INVALID_COMMENT_BODY(HttpStatus.BAD_REQUEST,
            "Comment body is required and must not exceed the allowed length."),
    INVALID_ATTACHMENT(HttpStatus.BAD_REQUEST,
            "Attachment metadata is invalid."),
    INVALID_PROJECT_IDENTIFIER(HttpStatus.BAD_REQUEST,
            "Project identifier must be 4-10 uppercase alphanumeric characters without reserved characters."),
    INVALID_TENANT_NAME(HttpStatus.BAD_REQUEST,
            "Tenant name is invalid."),
    INVALID_TENANT_SLUG(HttpStatus.BAD_REQUEST,
            "Tenant slug is invalid."),
    INVALID_HASH(HttpStatus.BAD_REQUEST,
            "Password hash is invalid."),

    // 404 Not Found - the addressed resource does not exist.
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Project not found."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "User not found."),
    INVALID_USERNAME(HttpStatus.NOT_FOUND,
            "Username is invalid."),

    // 409 Conflict - the request clashes with the current state of the resource.
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT,
            "Email is already registered."),
    USERNAME_GENERATION_FAILED(HttpStatus.CONFLICT,
            "Unable to generate a unique username."),

    // 401 Unauthorized - authentication is required or the provided credentials are rejected.
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,
            "Password does not meet the requirements.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    /** The wire value of this code, sent to clients as {@code ErrorResponse.code}. */
    public String code() {
        return name();
    }

    /** HTTP status the API responds with when this code is reported. */
    public HttpStatus status() {
        return status;
    }

    /** Fallback message used when an exception is created without a specific message. */
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Guards the invariant that a code only belongs to the exception category it was declared for,
     * so a mispaired code fails fast at construction instead of answering with a wrong status.
     *
     * @param expectedStatus status of the exception category using this code
     * @return this code, so it can be passed straight to a super constructor
     */
    public ErrorCode requireStatus(HttpStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                    "Error code %s declares status %s but was used with an exception that maps to %s"
                            .formatted(name(), status, expectedStatus));
        }
        return this;
    }
}
