--liquibase formatted sql

--changeset projile:002-create-users-table

CREATE TABLE users (
    user_id         UUID PRIMARY KEY,
    username        VARCHAR(255),
    user_email      VARCHAR(255) NOT NULL,
    hashed_password VARCHAR,
    display_name    VARCHAR(80),
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    oidc_issuer     VARCHAR(512),
    oidc_subject    VARCHAR(255),

    CONSTRAINT users_oidc_identity_unique UNIQUE (oidc_issuer, oidc_subject),
    CONSTRAINT users_oidc_identity_pair CHECK (
        (oidc_issuer IS NULL AND oidc_subject IS NULL)
        OR (oidc_issuer IS NOT NULL AND oidc_subject IS NOT NULL)),
    CONSTRAINT users_authentication_method CHECK (
        (NOT email_verified) OR hashed_password IS NOT NULL OR oidc_subject IS NOT NULL)
);

CREATE UNIQUE INDEX users_username_lower_unique ON users (lower(username));
CREATE UNIQUE INDEX users_email_lower_unique ON users (lower(user_email));

--rollback DROP TABLE users;