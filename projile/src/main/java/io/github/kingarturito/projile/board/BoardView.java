package io.github.kingarturito.projile.board;

import io.github.kingarturito.projile.project.ProjectId;

import java.time.Instant;

/**
 * Public read model for a board.
 */
public record BoardView(
        BoardId boardId,
        ProjectId projectId,
        String boardName,
        Instant createdAt,
        Instant updatedAt
) {}
