package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.UnauthorizedException;

public class InvalidUsernameException extends UnauthorizedException {
    public InvalidUsernameException(String message) {super(message);}
}
