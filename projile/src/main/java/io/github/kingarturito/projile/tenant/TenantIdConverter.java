package io.github.kingarturito.projile.tenant;

import jakarta.persistence.AttributeConverter;

import java.util.UUID;

public class TenantIdConverter implements AttributeConverter<TenantId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(TenantId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public TenantId convertToEntityAttribute(UUID attribute) {
        return attribute == null ? null : new TenantId(attribute);
    }
}
