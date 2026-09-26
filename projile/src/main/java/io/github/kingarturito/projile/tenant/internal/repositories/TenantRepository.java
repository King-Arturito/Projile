package io.github.kingarturito.projile.tenant.internal.repositories;

import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.tenant.internal.entities.Tenant;
import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantSlug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, TenantId> {

    Optional<Tenant> findByTenantSlug(TenantSlug tenantSlug);

    boolean existsByTenantSlug(TenantSlug tenantSlug);
}
