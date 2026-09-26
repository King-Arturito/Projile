package io.github.kingarturito.projile.task.internal.entities;

import io.github.kingarturito.projile.board.BoardColumnId;
import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.shared.Description;
import io.github.kingarturito.projile.shared.DescriptionConverter;
import io.github.kingarturito.projile.shared.Position;
import io.github.kingarturito.projile.shared.PositionConverter;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.task.internal.enums.TaskPriority;
import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

/**
 * A task lives in a board column; its workflow state is the column it sits in.
 * Ordering within the column uses a fractional index so moves never rewrite
 * sibling tasks. Completion is tracked explicitly via {@code completedAt}.
 */
@Entity
@Table(name = "task")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    private static final int TITLE_MAX_LENGTH = 255;

    @EmbeddedId
    @AttributeOverride(
            name = "taskId",
            column = @Column(name = "task_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private TaskId taskId = TaskId.newId();

    @Embedded
    @AttributeOverride(
            name = "tenantId",
            column = @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    )
    private TenantId tenantId;

    @Embedded
    @AttributeOverride(
            name = "boardId",
            column = @Column(name = "board_id", nullable = false, columnDefinition = "uuid")
    )
    private BoardId boardId;

    @Embedded
    @AttributeOverride(
            name = "columnId",
            column = @Column(name = "column_id", nullable = false, columnDefinition = "uuid")
    )
    private BoardColumnId columnId;

    @Column(name = "task_title", nullable = false, length = TITLE_MAX_LENGTH)
    private String taskTitle;

    @Column(name = "task_description", length = 2000)
    @Convert(converter = DescriptionConverter.class)
    private Description taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false, length = 16)
    private TaskPriority taskPriority = TaskPriority.MEDIUM;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "assignee_id", columnDefinition = "uuid")
    )
    private UserId assigneeId;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "position", nullable = false, length = 64)
    @Convert(converter = PositionConverter.class)
    private Position position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    private Task(TenantId tenantId, BoardId boardId, BoardColumnId columnId, String taskTitle, Position position) {
        this.tenantId = tenantId;
        this.boardId = boardId;
        this.columnId = columnId;
        this.taskTitle = taskTitle;
        this.position = position;
    }

    public static Task create(TenantId tenantId,
                              BoardId boardId,
                              BoardColumnId columnId,
                              String taskTitle,
                              Position position,
                              Clock clock) {
        validateTitle(taskTitle);
        Task task = new Task(tenantId, boardId, columnId, taskTitle.trim(), position);
        task.createdAt = Instant.now(clock);
        return task;
    }

    public void rename(String taskTitle, Clock clock) {
        validateTitle(taskTitle);
        this.taskTitle = taskTitle.trim();
        touch(clock);
    }

    public void changeDescription(Description taskDescription, Clock clock) {
        this.taskDescription = taskDescription;
        touch(clock);
    }

    public void changePriority(TaskPriority taskPriority, Clock clock) {
        this.taskPriority = taskPriority;
        touch(clock);
    }

    public void changeDueDate(Instant dueAt, Clock clock) {
        this.dueAt = dueAt;
        touch(clock);
    }

    public void assign(UserId assigneeId, Clock clock) {
        this.assigneeId = assigneeId;
        touch(clock);
    }

    public void unassign(Clock clock) {
        this.assigneeId = null;
        touch(clock);
    }

    public void moveTo(BoardColumnId columnId, Position position, Clock clock) {
        this.columnId = columnId;
        this.position = position;
        touch(clock);
    }

    public void complete(Clock clock) {
        this.completedAt = Instant.now(clock);
        touch(clock);
    }

    public void reopen(Clock clock) {
        this.completedAt = null;
        touch(clock);
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    private static void validateTitle(String taskTitle) {
        if (taskTitle == null || taskTitle.isBlank()) {
            throw new BadRequestException(ErrorCode.INVALID_TASK_TITLE, "Task title is required.");
        }
        if (taskTitle.trim().length() > TITLE_MAX_LENGTH) {
            throw new BadRequestException(ErrorCode.INVALID_TASK_TITLE,
                    "Task title must not exceed " + TITLE_MAX_LENGTH + " characters.");
        }
    }

    private void touch(Clock clock) {
        this.updatedAt = Instant.now(clock);
    }
}
