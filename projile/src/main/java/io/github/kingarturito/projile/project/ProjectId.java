package io.github.kingarturito.projile.project;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ProjectId(UUID projectId) {
    public static ProjectId newId() {
        return new ProjectId(UuidCreator.getTimeOrderedEpoch());
    }
}
