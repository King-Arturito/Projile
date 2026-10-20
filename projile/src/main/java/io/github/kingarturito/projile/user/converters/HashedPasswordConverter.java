package io.github.kingarturito.projile.user.converters;

import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class HashedPasswordConverter implements AttributeConverter<HashedPassword, String> {

    @Override
    public String convertToDatabaseColumn(HashedPassword hashedPassword) {
        return hashedPassword == null ? null : hashedPassword.value();
    }

    @Override
    public HashedPassword convertToEntityAttribute(String hashedPassword) {
        return  hashedPassword == null ? null : new HashedPassword(hashedPassword);
    }
}
