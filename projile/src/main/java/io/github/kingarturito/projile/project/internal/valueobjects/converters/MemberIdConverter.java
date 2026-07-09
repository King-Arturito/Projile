package io.github.kingarturito.projile.project.internal.valueobjects.converters;

import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import jakarta.persistence.AttributeConverter;

import java.util.UUID;

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
