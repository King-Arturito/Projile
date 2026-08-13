package io.github.kingarturito.projile.project.exceptions;

import io.github.kingarturito.projile.shared.exceptions.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
