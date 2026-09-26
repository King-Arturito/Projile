--liquibase formatted sql

--changeset you:009-create-board-table.sql

CREATE TABLE board (
    board_id   UUID PRIMARY KEY,
    project_id UUID         NOT NULL,
    tenant_id  UUID         NOT NULL,
    board_name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,

    -- one board per project for now; drop the constraint when multiple boards land
    CONSTRAINT uk_board_project UNIQUE (project_id)
);

CREATE INDEX idx_board_tenant_id ON board (tenant_id);

--rollback DROP TABLE board;
