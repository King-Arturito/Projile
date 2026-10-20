package io.github.kingarturito.projile.user.converters;

import io.github.kingarturito.projile.user.internal.valueobjects.DisplayName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DisplayNameConverter implements AttributeConverter<DisplayName, String> {
    @Override
    public String convertToDatabaseColumn(DisplayName displayName) {
        return displayName == null ? null : displayName.value();
    }

    @Override
    public DisplayName convertToEntityAttribute(String value) {
        return value == null ? null : new DisplayName(value);
    }
}
