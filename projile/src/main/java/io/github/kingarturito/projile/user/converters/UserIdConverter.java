package io.github.kingarturito.projile.user.converters;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = true)
public class UserIdConverter implements AttributeConverter<UserId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(UserId attribute) {
        return attribute == null ? null : attribute.value();
    }
    @Override
    public UserId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new UserId(dbData);
    }
}
