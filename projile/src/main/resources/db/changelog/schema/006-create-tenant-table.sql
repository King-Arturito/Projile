--liquibase formatted sql
--changeset you:006-create-tenant-table.sql

CREATE TABLE tenant (
    tenant_id     UUID PRIMARY KEY,
    tenant_name   VARCHAR(80) NOT NULL,
    tenant_slug   VARCHAR(40) NOT NULL,
    tenant_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ,
    CONSTRAINT uk_tenant_slug UNIQUE (tenant_slug),
    CONSTRAINT chk_tenant_status CHECK (tenant_status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

--rollback DROP TABLE tenant;