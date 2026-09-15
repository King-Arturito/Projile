--liquibase formatted sql
--changeset you:006-create-tenant-table.sql

CREATE TABLE tenant (
    tenant_id UUID PRIMARY KEY
);

--rollback DROP TABLE tenant;
