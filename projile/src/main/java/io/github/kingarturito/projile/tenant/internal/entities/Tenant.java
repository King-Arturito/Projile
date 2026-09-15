package io.github.kingarturito.projile.tenant.internal.entities;

import io.github.kingarturito.projile.tenant.TenantId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
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

    

}
