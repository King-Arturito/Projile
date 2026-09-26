package io.github.kingarturito.projile.tenant.internal.repositories;

import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.tenant.internal.entities.TenantMember;
import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantMemberId;
import io.github.kingarturito.projile.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantMemberRepository extends JpaRepository<TenantMember, TenantMemberId> {

    Optional<TenantMember> findByTenantIdAndUserId(TenantId tenantId, UserId userId);

    List<TenantMember> findAllByUserId(UserId userId);

    List<TenantMember> findAllByTenantId(TenantId tenantId);
}
