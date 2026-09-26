package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ConflictException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class UsernameGeneratorException extends ConflictException {

    public UsernameGeneratorException() {
        super(ErrorCode.USERNAME_GENERATION_FAILED);
    }

    public UsernameGeneratorException(String message) {
        super(ErrorCode.USERNAME_GENERATION_FAILED, message);
    }
}
