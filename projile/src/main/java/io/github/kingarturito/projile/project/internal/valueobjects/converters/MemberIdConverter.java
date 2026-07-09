package io.github.kingarturito.projile.project.internal.valueobjects.converters;

import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = true)
public class MemberIdConverter implements AttributeConverter<MemberId, UUID> {
    @Override
    public UUID convertToDatabaseColumn(MemberId memberId) {
        return memberId  ==  null ? null : memberId.value();
    }

    @Override
    public MemberId convertToEntityAttribute(UUID memberId) {
        return  memberId == null ? null : new MemberId(memberId);
    }
}
