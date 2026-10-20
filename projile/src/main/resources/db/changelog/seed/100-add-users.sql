--liquibase formatted sql

--changeset projile:100-add-users context:dev

INSERT INTO users (user_id, username, user_email, hashed_password, display_name, email_verified) VALUES
    ('01a033a2-5291-71b8-a9a3-27d67d306203', 'admin1', 'admin1@dev.local', '$argon2id$v=19$m=16384,t=2,p=1$THI4V1hVQTR2UUtNOUlCNw$HKMreA+z2o5uhnyUdjKQgicEiE0Myu9gMjWHAUO6+9U', 'Admin One', TRUE),
    ('01a033a3-982b-76ed-a256-17ce8e266466', 'admin2', 'admin2@dev.local', '$argon2id$v=19$m=16384,t=2,p=1$WHl0RXpTRHJMNE44OW5ZTw$OvHmm4QC+F8K5gs6s81VazYSmMtP+yG+OjV4YmG6K4A', 'Admin Two', TRUE);

--rollback DELETE FROM users WHERE username IN ('admin1', 'admin2')