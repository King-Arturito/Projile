--liquibase formatted sql

--changeset you:007-create-attachment-table.sql

CREATE TABLE attachment (
    attachment_id UUID PRIMARY KEY,
    task_id       UUID         NOT NULL,
    uploaded_by   UUID         NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(128) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    storage_key   VARCHAR(512) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT chk_attachment_size CHECK (size_bytes > 0)
);

CREATE INDEX idx_attachment_task_id ON attachment (task_id);

--rollback DROP TABLE attachment;
