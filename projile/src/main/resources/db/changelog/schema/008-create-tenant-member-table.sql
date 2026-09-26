--liquibase formatted sql

--changeset you:008-create-tenant-member-table.sql

CREATE TABLE tenant_member (
    tenant_member_id UUID PRIMARY KEY,
    tenant_id        UUID        NOT NULL,
    user_id          UUID        NOT NULL,
    tenant_role      VARCHAR(32) NOT NULL,
    member_status    VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    joined_at        TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_tenant_member_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT chk_tenant_member_role CHECK (tenant_role IN ('OWNER', 'ADMIN', 'MEMBER')),
    CONSTRAINT chk_tenant_member_status CHECK (member_status IN ('INVITED', 'ACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_tenant_member_tenant_id ON tenant_member (tenant_id);
CREATE INDEX idx_tenant_member_user_id ON tenant_member (user_id);

--rollback DROP TABLE tenant_member;
