package io.github.kingarturito.projile.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DescriptionConverter implements AttributeConverter<Description, String> {
    @Override
    public String convertToDatabaseColumn(Description description) {
        return description == null ? null : description.value();
    }

    @Override
    public Description convertToEntityAttribute(String description) {
        return description == null ? null : new Description(description);
    }
}
