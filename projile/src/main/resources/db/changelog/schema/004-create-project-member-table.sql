
-- liquibase formatted sql
-- changeset you:004-create-project-member-table.sql

CREATE TABLE project_member(
    member_id uuid PRIMARY KEY,
    project_id uuid NOT NULL,
    user_id uuid NOT NULL,
    project_role VARCHAR(255),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    invited_by uuid,
    invitation_message VARCHAR(1000),

    CHECK (project_role in ('OWNER', 'MANAGER', 'MEMBER', 'VIEWER')),
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id)

);



-- Rollback drop table project_member;