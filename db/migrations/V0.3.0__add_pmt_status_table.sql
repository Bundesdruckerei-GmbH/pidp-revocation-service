--
-- Copyright 2024-2026 Bundesdruckerei GmbH
-- For the license see the accompanying file LICENSE.MD.
--

CREATE TYPE lifecycle_status AS ENUM ('VALID', 'INVALID');

CREATE TABLE IF NOT EXISTS pid_master_token_status
(
    id              BIGINT                      PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    token_id        VARCHAR                     UNIQUE NOT NULL,
    pseudonym       VARCHAR                     NOT NULL,
    status          lifecycle_status            DEFAULT 'VALID' NOT NULL,
    expiration      TIMESTAMP WITH TIME ZONE    NOT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON pid_master_token_status TO ${APP_USER};

CREATE OR REPLACE FUNCTION prevent_update_on_invalid()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'INVALID' THEN
        RAISE EXCEPTION 'Cannot change status that is INVALID';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_lifecycle_status
    BEFORE UPDATE ON pid_master_token_status
    FOR EACH ROW
    EXECUTE FUNCTION prevent_update_on_invalid();
