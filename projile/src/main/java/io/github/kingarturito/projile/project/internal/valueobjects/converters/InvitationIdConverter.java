package io.github.kingarturito.projile.project.internal.valueobjects.converters;

import io.github.kingarturito.projile.project.internal.valueobjects.InvitationId;
import jakarta.persistence.AttributeConverter;

import java.util.UUID;

public class InvitationIdConverter implements AttributeConverter<InvitationId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(InvitationId invitationId) {
        return invitationId.value();
    }

    @Override
    public InvitationId convertToEntityAttribute(UUID uuid) {
        return new InvitationId(uuid);
    }
}
