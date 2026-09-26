--liquibase formatted sql

--changeset you:011-create-task-table.sql

CREATE TABLE task (
    task_id          UUID PRIMARY KEY,
    tenant_id        UUID          NOT NULL,
    board_id         UUID          NOT NULL,
    column_id        UUID          NOT NULL,
    task_title       VARCHAR(255)  NOT NULL,
    task_description VARCHAR(2000),
    task_priority    VARCHAR(16)   NOT NULL DEFAULT 'MEDIUM',
    assignee_id      UUID,
    due_at           TIMESTAMPTZ,
    position         VARCHAR(64)   NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,

    CONSTRAINT chk_task_priority CHECK (task_priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_task_tenant_id ON task (tenant_id);
CREATE INDEX idx_task_board_id ON task (board_id);
CREATE INDEX idx_task_column_id_position ON task (column_id, position);
CREATE INDEX idx_task_assignee_id ON task (assignee_id);

--rollback DROP TABLE task;
