package io.github.kingarturito.projile.user.converters;

import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UsernameConverter implements AttributeConverter<Username, String> {
    @Override
    public String convertToDatabaseColumn(Username username) {
        return username == null ? null : username.value();
    }

    @Override
    public Username convertToEntityAttribute(String usernameString) {
        return usernameString == null ? null : new Username(usernameString);
    }
}
