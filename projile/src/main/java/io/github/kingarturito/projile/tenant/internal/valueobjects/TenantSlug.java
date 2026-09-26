package io.github.kingarturito.projile.tenant.internal.valueobjects;

import io.github.kingarturito.projile.tenant.internal.exceptions.InvalidTenantSlugException;

import java.util.regex.Pattern;

public record TenantSlug(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 40;
    private static final Pattern VALID_FORMAT = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    public TenantSlug {
        if (value == null) {
            throw new InvalidTenantSlugException("Tenant slug is required.");
        }
        value = value.trim().toLowerCase();
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH || !VALID_FORMAT.matcher(value).matches()) {
            throw new InvalidTenantSlugException(
                    "Tenant slug must be " + MIN_LENGTH + "-" + MAX_LENGTH
                            + " characters of lowercase letters, digits and single hyphens.");
        }
    }
}
