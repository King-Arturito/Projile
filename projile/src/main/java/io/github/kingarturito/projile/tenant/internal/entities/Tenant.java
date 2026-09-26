package io.github.kingarturito.projile.tenant.internal.entities;

import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.tenant.internal.enums.TenantStatus;
import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantName;
import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantSlug;
import io.github.kingarturito.projile.tenant.internal.valueobjects.converters.TenantNameConverter;
import io.github.kingarturito.projile.tenant.internal.valueobjects.converters.TenantSlugConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "tenant", uniqueConstraints = @UniqueConstraint(name = "uk_tenant_slug", columnNames = "tenant_slug"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tenant {

    @EmbeddedId
    @AttributeOverride(
            name = "tenantId",
            column = @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private TenantId tenantId = TenantId.newId();

    @Column(name = "tenant_name", nullable = false, length = 80)
    @Convert(converter = TenantNameConverter.class)
    private TenantName tenantName;

    @Column(name = "tenant_slug", nullable = false, length = 40)
    @Convert(converter = TenantSlugConverter.class)
    private TenantSlug tenantSlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_status", nullable = false, length = 32)
    private TenantStatus tenantStatus = TenantStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private Tenant(TenantName tenantName, TenantSlug tenantSlug) {
        this.tenantName = tenantName;
        this.tenantSlug = tenantSlug;
    }

    public static Tenant create(TenantName tenantName, TenantSlug tenantSlug, Clock clock) {
        Tenant tenant = new Tenant(tenantName, tenantSlug);
        tenant.createdAt = Instant.now(clock);
        return tenant;
    }

    public void rename(TenantName tenantName, Clock clock) {
        this.tenantName = tenantName;
        this.updatedAt = Instant.now(clock);
    }

    public void suspend(Clock clock) {
        changeStatus(TenantStatus.SUSPENDED, clock);
    }

    public void reactivate(Clock clock) {
        changeStatus(TenantStatus.ACTIVE, clock);
    }

    public void archive(Clock clock) {
        changeStatus(TenantStatus.ARCHIVED, clock);
    }

    private void changeStatus(TenantStatus tenantStatus, Clock clock) {
        this.tenantStatus = tenantStatus;
        this.updatedAt = Instant.now(clock);
    }
}
