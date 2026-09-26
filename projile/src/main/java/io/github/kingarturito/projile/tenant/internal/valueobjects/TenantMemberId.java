package io.github.kingarturito.projile.tenant.internal.valueobjects;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public record TenantMemberId(UUID tenantMemberId) {
    public static TenantMemberId newId() {
        return new TenantMemberId(UuidCreator.getTimeOrderedEpoch());
    }
}
