package io.github.kingarturito.projile.tenant.internal.valueobjects;

import io.github.kingarturito.projile.tenant.internal.exceptions.InvalidTenantNameException;

public record TenantName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 80;

    public TenantName {
        if (value == null) {
            throw new InvalidTenantNameException("Tenant name is required.");
        }
        value = value.trim();
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidTenantNameException(
                    "Tenant name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }
    }
}
