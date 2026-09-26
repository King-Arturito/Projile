package io.github.kingarturito.projile.project.exceptions;

import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.shared.exceptions.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException() {
        super(ErrorCode.PROJECT_NOT_FOUND);
    }

    public ProjectNotFoundException(String message) {
        super(ErrorCode.PROJECT_NOT_FOUND, message);
    }
}
