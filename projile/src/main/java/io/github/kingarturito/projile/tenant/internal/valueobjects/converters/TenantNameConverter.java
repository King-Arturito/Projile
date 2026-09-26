package io.github.kingarturito.projile.tenant.internal.valueobjects.converters;

import io.github.kingarturito.projile.tenant.internal.valueobjects.TenantName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TenantNameConverter implements AttributeConverter<TenantName, String> {

    @Override
    public String convertToDatabaseColumn(TenantName tenantName) {
        return tenantName == null ? null : tenantName.value();
    }

    @Override
    public TenantName convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new TenantName(dbData);
    }
}
