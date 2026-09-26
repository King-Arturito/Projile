package io.github.kingarturito.projile.task;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TaskId(UUID taskId) {
    public static TaskId newId() {
        return new TaskId(UuidCreator.getTimeOrderedEpoch());
    }
}
