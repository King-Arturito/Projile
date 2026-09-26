package io.github.kingarturito.projile.task;

import io.github.kingarturito.projile.board.BoardColumnId;
import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.user.UserId;

import java.time.Instant;

/**
 * Public read model for a task.
 */
public record TaskView(
        TaskId taskId,
        BoardId boardId,
        BoardColumnId columnId,
        String taskTitle,
        String taskDescription,
        String taskPriority,
        UserId assigneeId,
        Instant dueAt,
        String position,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {}
