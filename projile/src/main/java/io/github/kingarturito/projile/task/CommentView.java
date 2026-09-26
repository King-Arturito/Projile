package io.github.kingarturito.projile.task;

import io.github.kingarturito.projile.user.UserId;

import java.time.Instant;

/**
 * Public read model for a task comment.
 */
public record CommentView(
        CommentId commentId,
        TaskId taskId,
        UserId authorId,
        String body,
        Instant createdAt,
        Instant editedAt
) {}
