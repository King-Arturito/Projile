package io.github.kingarturito.projile.board;

import java.time.Instant;

/**
 * Public read model for a board column, including its fractional
 * ordering key so clients can render and reorder lanes.
 */
public record BoardColumnView(
        BoardColumnId columnId,
        BoardId boardId,
        String columnName,
        String position,
        Instant createdAt,
        Instant updatedAt
) {}
