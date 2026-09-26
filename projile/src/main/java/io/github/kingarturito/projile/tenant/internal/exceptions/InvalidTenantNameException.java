package io.github.kingarturito.projile.tenant.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidTenantNameException extends BadRequestException {

    public InvalidTenantNameException() {
        super(ErrorCode.INVALID_TENANT_NAME);
    }

    public InvalidTenantNameException(String message) {
        super(ErrorCode.INVALID_TENANT_NAME, message);
    }
}
