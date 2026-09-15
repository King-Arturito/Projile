package io.github.kingarturito.projile.project.internal.valueobjects;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public record InvitationId(UUID invitationId) {
    public static InvitationId newId() {
        return new InvitationId(UuidCreator.getTimeOrderedEpoch());
    }
}