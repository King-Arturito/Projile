package io.github.kingarturito.projile.board;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record BoardId(UUID boardId) {
    public static BoardId newId() {
        return new BoardId(UuidCreator.getTimeOrderedEpoch());
    }
}
