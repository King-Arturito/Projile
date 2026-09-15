package io.github.kingarturito.projile.user;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record UserId(UUID userId) {
    public static UserId newId() {
        return new UserId(UuidCreator.getTimeOrderedEpoch());
    }
}