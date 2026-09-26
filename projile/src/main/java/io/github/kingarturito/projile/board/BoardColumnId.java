package io.github.kingarturito.projile.board;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record BoardColumnId(UUID columnId) {
    public static BoardColumnId newId() {
        return new BoardColumnId(UuidCreator.getTimeOrderedEpoch());
    }
}
