--liquibase formatted sql

--changeset projile:013-create-email-verification-table
CREATE TABLE email_verifications (
    user_id UUID PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX email_verifications_expiry_idx ON email_verifications(expires_at);

--rollback DROP TABLE email_verifications;
