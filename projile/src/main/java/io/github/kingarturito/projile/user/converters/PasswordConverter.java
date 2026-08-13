package io.github.kingarturito.projile.user.converters;

import io.github.kingarturito.projile.user.internal.valueobjects.Password;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PasswordConverter implements AttributeConverter<Password,String> {

    @Override
    public String convertToDatabaseColumn(Password password) {
        return password ==  null ? null : password.value();
    }

    @Override
    public Password convertToEntityAttribute(String s) {
        return s == null ? null : new Password(s);
    }
}
