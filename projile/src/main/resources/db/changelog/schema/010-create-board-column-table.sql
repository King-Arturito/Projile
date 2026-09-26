--liquibase formatted sql

--changeset you:010-create-board-column-table.sql

CREATE TABLE board_column (
    column_id   UUID PRIMARY KEY,
    board_id    UUID        NOT NULL,
    column_name VARCHAR(80) NOT NULL,
    position    VARCHAR(64) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ
);

-- position is a LexoRank-style fractional index: ordering never rewrites neighbours
CREATE INDEX idx_board_column_board_id_position ON board_column (board_id, position);

--rollback DROP TABLE board_column;
