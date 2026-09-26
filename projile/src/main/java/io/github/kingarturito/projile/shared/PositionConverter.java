package io.github.kingarturito.projile.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PositionConverter implements AttributeConverter<Position, String> {

    @Override
    public String convertToDatabaseColumn(Position position) {
        return position == null ? null : position.value();
    }

    @Override
    public Position convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Position(dbData);
    }
}
