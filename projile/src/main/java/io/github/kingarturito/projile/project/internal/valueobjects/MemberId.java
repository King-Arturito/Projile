package io.github.kingarturito.projile.project.internal.valueobjects;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public record MemberId(UUID memberId) {
    public static MemberId newId() {
        return new MemberId(UuidCreator.getTimeOrderedEpoch());
    }
}
