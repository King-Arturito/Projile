package io.github.kingarturito.projile.tenant;

import io.github.kingarturito.projile.user.UserId;

import java.time.Instant;

/**
 * Public read model for a user's membership in a tenant.
 */
public record TenantMemberView(
        TenantId tenantId,
        UserId userId,
        String tenantRole,
        String memberStatus,
        Instant joinedAt
) {}
