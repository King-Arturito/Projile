package io.github.kingarturito.projile.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DescriptionConverter implements AttributeConverter<Description, String> {
    @Override
    public String convertToDatabaseColumn(Description description) {
        return description.value();
    }

    public Description convertToEntityAttribute(String description) {
        return new Description(description);
    }
}
