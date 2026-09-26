package io.github.kingarturito.projile.board.internal.entities;

import io.github.kingarturito.projile.board.BoardColumnId;
import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.shared.Position;
import io.github.kingarturito.projile.shared.PositionConverter;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

/**
 * An ordered lane within a board ("To Do", "In Progress", ...).
 * Ordering is stored as a fractional index so drag & drop never rewrites
 * the neighbouring columns.
 */
@Entity
@Table(name = "board_column")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BoardColumn {

    private static final int NAME_MAX_LENGTH = 80;

    @EmbeddedId
    @AttributeOverride(
            name = "columnId",
            column = @Column(name = "column_id", nullable = false, columnDefinition = "uuid")
    )
    @EqualsAndHashCode.Include
    private BoardColumnId columnId = BoardColumnId.newId();

    @Embedded
    @AttributeOverride(
            name = "boardId",
            column = @Column(name = "board_id", nullable = false, columnDefinition = "uuid")
    )
    private BoardId boardId;

    @Column(name = "column_name", nullable = false, length = NAME_MAX_LENGTH)
    private String columnName;

    @Column(name = "position", nullable = false, length = 64)
    @Convert(converter = PositionConverter.class)
    private Position position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private BoardColumn(BoardId boardId, String columnName, Position position) {
        this.boardId = boardId;
        this.columnName = columnName;
        this.position = position;
    }

    public static BoardColumn create(BoardId boardId, String columnName, Position position, Clock clock) {
        validateName(columnName);
        BoardColumn column = new BoardColumn(boardId, columnName.trim(), position);
        column.createdAt = Instant.now(clock);
        return column;
    }

    public void rename(String columnName, Clock clock) {
        validateName(columnName);
        this.columnName = columnName.trim();
        this.updatedAt = Instant.now(clock);
    }

    public void reposition(Position position, Clock clock) {
        this.position = position;
        this.updatedAt = Instant.now(clock);
    }

    private static void validateName(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            throw new BadRequestException(ErrorCode.INVALID_COLUMN_NAME, "Column name is required.");
        }
        if (columnName.trim().length() > NAME_MAX_LENGTH) {
            throw new BadRequestException(ErrorCode.INVALID_COLUMN_NAME,
                    "Column name must not exceed " + NAME_MAX_LENGTH + " characters.");
        }
    }
}
