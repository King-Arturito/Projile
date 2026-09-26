package io.github.kingarturito.projile.attachment.internal.entities;

import io.github.kingarturito.projile.attachment.AttachmentId;
import io.github.kingarturito.projile.attachment.exceptions.InvalidAttachmentException;
import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "attachment")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Attachment {

    private static final int FILE_NAME_MAX_LENGTH = 255;
    private static final int CONTENT_TYPE_MAX_LENGTH = 128;
    private static final int STORAGE_KEY_MAX_LENGTH = 512;

    @EmbeddedId
    @AttributeOverride(
            name = "attachmentId",
            column = @Column(name = "attachment_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private AttachmentId attachmentId = AttachmentId.newId();

    @Embedded
    @AttributeOverride(
            name = "taskId",
            column = @Column(name = "task_id", nullable = false, columnDefinition = "uuid")
    )
    private TaskId taskId;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "uploaded_by", nullable = false, columnDefinition = "uuid")
    )
    private UserId uploadedBy;

    @Column(name = "file_name", nullable = false, length = FILE_NAME_MAX_LENGTH)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = CONTENT_TYPE_MAX_LENGTH)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "storage_key", nullable = false, length = STORAGE_KEY_MAX_LENGTH)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private Attachment(TaskId taskId, UserId uploadedBy, String fileName, String contentType, long sizeBytes, String storageKey) {
        this.taskId = taskId;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
    }

    public static Attachment register(TaskId taskId,
                                      UserId uploadedBy,
                                      String fileName,
                                      String contentType,
                                      long sizeBytes,
                                      String storageKey,
                                      Clock clock) {
        validateMetadata(fileName, contentType, sizeBytes, storageKey);
        Attachment attachment = new Attachment(taskId, uploadedBy, fileName.trim(), contentType.trim(), sizeBytes, storageKey);
        attachment.createdAt = Instant.now(clock);
        return attachment;
    }

    private static void validateMetadata(String fileName, String contentType, long sizeBytes, String storageKey) {
        if (fileName == null || fileName.isBlank() || fileName.trim().length() > FILE_NAME_MAX_LENGTH) {
            throw new InvalidAttachmentException("File name is required and must not exceed " + FILE_NAME_MAX_LENGTH + " characters.");
        }
        if (contentType == null || contentType.isBlank() || contentType.trim().length() > CONTENT_TYPE_MAX_LENGTH) {
            throw new InvalidAttachmentException("Content type is required and must not exceed " + CONTENT_TYPE_MAX_LENGTH + " characters.");
        }
        if (sizeBytes <= 0) {
            throw new InvalidAttachmentException("Attachment size must be greater than zero.");
        }
        if (storageKey == null || storageKey.isBlank() || storageKey.length() > STORAGE_KEY_MAX_LENGTH) {
            throw new InvalidAttachmentException("Storage key is required and must not exceed " + STORAGE_KEY_MAX_LENGTH + " characters.");
        }
    }
}
