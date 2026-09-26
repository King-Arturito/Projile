package io.github.kingarturito.projile.tenant.internal.valueobjects.converters;

import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantSlug;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TenantSlugConverter implements AttributeConverter<TenantSlug, String> {

    @Override
    public String convertToDatabaseColumn(TenantSlug tenantSlug) {
        return tenantSlug == null ? null : tenantSlug.value();
    }

    @Override
    public TenantSlug convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new TenantSlug(dbData);
    }
}
