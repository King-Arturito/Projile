package io.github.kingarturito.projile.tenant;

import java.time.Instant;

/**
 * Public read model for a tenant (workspace).
 */
public record TenantView(
        TenantId tenantId,
        String tenantName,
        String tenantSlug,
        String tenantStatus,
        Instant createdAt
) {}
