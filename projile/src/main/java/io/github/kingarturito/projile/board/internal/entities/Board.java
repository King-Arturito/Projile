package io.github.kingarturito.projile.board.internal.entities;

import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.tenant.TenantId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

/**
 * A board belongs to exactly one project and owns its columns.
 * The tenant id is carried for tenant-scoped queries and defence in depth.
 */
@Entity
@Table(name = "board", uniqueConstraints = @UniqueConstraint(name = "uk_board_project", columnNames = "project_id"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Board {

    private static final int NAME_MAX_LENGTH = 120;

    @EmbeddedId
    @AttributeOverride(
            name = "boardId",
            column = @Column(name = "board_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private BoardId boardId = BoardId.newId();

    @Embedded
    @AttributeOverride(
            name = "projectId",
            column = @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    )
    private ProjectId projectId;

    @Embedded
    @AttributeOverride(
            name = "tenantId",
            column = @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    )
    private TenantId tenantId;

    @Column(name = "board_name", nullable = false, length = NAME_MAX_LENGTH)
    private String boardName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private Board(ProjectId projectId, TenantId tenantId, String boardName) {
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.boardName = boardName;
    }

    public static Board create(ProjectId projectId, TenantId tenantId, String boardName, Clock clock) {
        validateName(boardName);
        Board board = new Board(projectId, tenantId, boardName.trim());
        board.createdAt = Instant.now(clock);
        return board;
    }

    public void rename(String boardName, Clock clock) {
        validateName(boardName);
        this.boardName = boardName.trim();
        this.updatedAt = Instant.now(clock);
    }

    private static void validateName(String boardName) {
        if (boardName == null || boardName.isBlank()) {
            throw new BadRequestException(ErrorCode.INVALID_BOARD_NAME, "Board name is required.");
        }
        if (boardName.trim().length() > NAME_MAX_LENGTH) {
            throw new BadRequestException(ErrorCode.INVALID_BOARD_NAME,
                    "Board name must not exceed " + NAME_MAX_LENGTH + " characters.");
        }
    }
}
