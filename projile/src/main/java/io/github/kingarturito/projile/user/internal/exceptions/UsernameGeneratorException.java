package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ConflictException;

public class UsernameGeneratorException extends ConflictException {
    public UsernameGeneratorException(String message) {
        super(message);
    }
}
