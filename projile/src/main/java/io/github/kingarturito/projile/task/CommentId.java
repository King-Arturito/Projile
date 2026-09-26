package io.github.kingarturito.projile.task;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record CommentId(UUID commentId) {
    public static CommentId newId() {
        return new CommentId(UuidCreator.getTimeOrderedEpoch());
    }
}
