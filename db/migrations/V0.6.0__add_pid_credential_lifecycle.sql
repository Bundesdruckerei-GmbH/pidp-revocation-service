--
-- Copyright 2024-2026 Bundesdruckerei GmbH
-- For the license see the accompanying file LICENSE.MD.
--

CREATE TABLE IF NOT EXISTS pid_credential_lifecycle
(
    id                          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    list_id                     VARCHAR                                         NOT NULL,
    list_index                  INT                                             NOT NULL,
    status                      lifecycle_status                                DEFAULT 'VALID' NOT NULL,
    expiration_time             TIMESTAMP WITH TIME ZONE                        NOT NULL,
    pid_master_token_status_id  BIGINT                                          NOT NULL,
    FOREIGN KEY (pid_master_token_status_id) REFERENCES pid_master_token_status (id),
    CONSTRAINT uq_pid_credential_lifecycle_list_id_list_index UNIQUE (list_id, list_index)
);
CREATE INDEX IF NOT EXISTS pid_credential_lifecycle_pid_master_token_status_id_idx ON pid_credential_lifecycle (pid_master_token_status_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON pid_credential_lifecycle TO ${APP_USER};


CREATE TRIGGER check_pid_lifecycle_status
    BEFORE UPDATE ON pid_credential_lifecycle
    FOR EACH ROW
    EXECUTE FUNCTION prevent_update_on_invalid();
