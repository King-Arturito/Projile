--liquibase formatted sql
--changeset you:003-create-projects-table.sql

CREATE TABLE project (
    project_id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    project_description VARCHAR(2000),
    project_identifier VARCHAR(10) NOT NULL,
    project_status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE,


    CHECK (project_status IN ('IN_PROGRESS', 'COMPLETED', 'ARCHIVED')),

    CONSTRAINT uk_project_tenant_identifier UNIQUE (tenant_id, project_identifier)
);


--rollback DROP TABLE projects;