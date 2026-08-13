package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.UnauthorizedException;

public class InvalidPasswordException extends UnauthorizedException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
