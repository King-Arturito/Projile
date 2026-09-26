package io.github.kingarturito.projile.task.internal.entities;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.task.CommentId;
import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {

    private static final int BODY_MAX_LENGTH = 4000;

    @EmbeddedId
    @AttributeOverride(
            name = "commentId",
            column = @Column(name = "comment_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private CommentId commentId = CommentId.newId();

    @Embedded
    @AttributeOverride(
            name = "taskId",
            column = @Column(name = "task_id", nullable = false, columnDefinition = "uuid")
    )
    private TaskId taskId;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "author_id", nullable = false, columnDefinition = "uuid")
    )
    private UserId authorId;

    @Column(name = "body", nullable = false, length = BODY_MAX_LENGTH)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    private Comment(TaskId taskId, UserId authorId, String body) {
        this.taskId = taskId;
        this.authorId = authorId;
        this.body = body;
    }

    public static Comment write(TaskId taskId, UserId authorId, String body, Clock clock) {
        validateBody(body);
        Comment comment = new Comment(taskId, authorId, body.trim());
        comment.createdAt = Instant.now(clock);
        return comment;
    }

    public void edit(String body, Clock clock) {
        validateBody(body);
        this.body = body.trim();
        this.editedAt = Instant.now(clock);
    }

    private static void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException(ErrorCode.INVALID_COMMENT_BODY, "Comment body is required.");
        }
        if (body.trim().length() > BODY_MAX_LENGTH) {
            throw new BadRequestException(ErrorCode.INVALID_COMMENT_BODY,
                    "Comment body must not exceed " + BODY_MAX_LENGTH + " characters.");
        }
    }
}
