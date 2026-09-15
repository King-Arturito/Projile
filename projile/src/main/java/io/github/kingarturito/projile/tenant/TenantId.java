package io.github.kingarturito.projile.tenant;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public record TenantId(UUID tenantId) {
    public static TenantId newId() {
        return new TenantId(UuidCreator.getTimeOrderedEpoch());
    }
}
