--liquibase formatted sql

--changeset you:012-create-comment-table.sql

CREATE TABLE comment (
    comment_id UUID PRIMARY KEY,
    task_id    UUID          NOT NULL,
    author_id  UUID          NOT NULL,
    body       VARCHAR(4000) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL,
    edited_at  TIMESTAMPTZ
);

CREATE INDEX idx_comment_task_id_created_at ON comment (task_id, created_at);

--rollback DROP TABLE comment;
