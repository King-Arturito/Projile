package io.github.kingarturito.projile.tenant.internal.exceptions;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;

public class InvalidTenantSlugException extends BadRequestException {

    public InvalidTenantSlugException() {
        super(ErrorCode.INVALID_TENANT_SLUG);
    }

    public InvalidTenantSlugException(String message) {
        super(ErrorCode.INVALID_TENANT_SLUG, message);
    }
}
