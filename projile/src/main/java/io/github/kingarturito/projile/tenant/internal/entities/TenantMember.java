package io.github.kingarturito.projile.tenant.internal.entities;

import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.tenant.internal.enums.TenantMemberStatus;
import io.github.kingarturito.projile.tenant.internal.enums.TenantRole;
import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantMemberId;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "tenant_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_member_tenant_user", columnNames = {"tenant_id", "user_id"}))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TenantMember {

    @EmbeddedId
    @AttributeOverride(
            name = "tenantMemberId",
            column = @Column(name = "tenant_member_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private TenantMemberId tenantMemberId = TenantMemberId.newId();

    @Embedded
    @AttributeOverride(
            name = "tenantId",
            column = @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    )
    private TenantId tenantId;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    )
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_role", nullable = false, length = 32)
    private TenantRole tenantRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 32)
    private TenantMemberStatus memberStatus = TenantMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    private TenantMember(TenantId tenantId, UserId userId, TenantRole tenantRole) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.tenantRole = tenantRole;
    }

    public static TenantMember add(TenantId tenantId, UserId userId, TenantRole tenantRole, Clock clock) {
        TenantMember member = new TenantMember(tenantId, userId, tenantRole);
        member.joinedAt = Instant.now(clock);
        return member;
    }

    public void changeRole(TenantRole tenantRole) {
        this.tenantRole = tenantRole;
    }

    public void suspend() {
        this.memberStatus = TenantMemberStatus.SUSPENDED;
    }

    public void reactivate() {
        this.memberStatus = TenantMemberStatus.ACTIVE;
    }
}
