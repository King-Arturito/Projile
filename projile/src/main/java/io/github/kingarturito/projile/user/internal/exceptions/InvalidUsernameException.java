package io.github.kingarturito.projile.user.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.NotFoundException;

public class InvalidUsernameException extends NotFoundException {
    public InvalidUsernameException(String message) {super(message);}
}
