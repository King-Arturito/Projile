--liquibase formatted sql

--changeset you:002-create-users-table.sql

CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    user_email VARCHAR(255) UNIQUE,
    hashed_password VARCHAR NOT NULL
);

--rollback DROP TABLE users;