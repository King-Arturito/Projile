package io.github.kingarturito.projile.attachment.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidAttachmentException extends BadRequestException {

    public InvalidAttachmentException() {
        super(ErrorCode.INVALID_ATTACHMENT);
    }

    public InvalidAttachmentException(String message) {
        super(ErrorCode.INVALID_ATTACHMENT, message);
    }
}
