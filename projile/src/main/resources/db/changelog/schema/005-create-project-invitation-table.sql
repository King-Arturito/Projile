-- liquibase formatted sql
-- changeset you:005.create-project-invitation-table.sql

CREATE TABLE project_invitation
(
    invitation_id       UUID         NOT NULL,
    project_id          UUID         NOT NULL,
    inviter_member_id   UUID         NOT NULL,
    invited_user_id     UUID         NOT NULL,
    invited_at          TIMESTAMPTZ  NOT NULL,
    expires_at          TIMESTAMPTZ  NOT NULL,
    invitation_status   VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    invitation_message VARCHAR(1000),

    CHECK (invitation_status in ('ACCEPTED','REJECTED', 'PENDING', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT pk_project_invitation PRIMARY KEY (invitation_id)
);

CREATE INDEX idx_project_invitation_project_id ON project_invitation (project_id);
CREATE INDEX idx_project_invitation_invited_user_id ON project_invitation (invited_user_id);

--rollback DROP TABLE project_invitation;
