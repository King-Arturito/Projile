package io.github.kingarturito.projile.attachment;

import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.user.UserId;

import java.time.Instant;

/**
 * Public read model for a task attachment (metadata only; the binary is
 * fetched from object storage through the storage abstraction).
 */
public record AttachmentView(
        AttachmentId attachmentId,
        TaskId taskId,
        UserId uploadedBy,
        String fileName,
        String contentType,
        long sizeBytes,
        Instant createdAt
) {}
