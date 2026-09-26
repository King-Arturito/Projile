package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ConflictException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class EmailAlreadyRegisteredException extends ConflictException {

    public EmailAlreadyRegisteredException() {
        super(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }

    public EmailAlreadyRegisteredException(String message) {
        super(ErrorCode.EMAIL_ALREADY_REGISTERED, message);
    }
}
