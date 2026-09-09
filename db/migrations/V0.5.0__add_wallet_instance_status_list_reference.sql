--
-- Copyright 2024-2026 Bundesdruckerei GmbH
-- For the license see the accompanying file LICENSE.MD.
--

ALTER TABLE pid_master_token_status
    ADD COLUMN wallet_instance_reference VARCHAR;

CREATE TABLE IF NOT EXISTS status_list_reference
(
    id  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    uri VARCHAR                                         NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS status_list_reference_uri_idx ON status_list_reference (uri);
GRANT SELECT, INSERT, UPDATE, DELETE ON status_list_reference TO ${APP_USER};

CREATE TABLE IF NOT EXISTS wallet_instance_status_reference
(
    id                        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    wallet_instance_reference VARCHAR                                         NOT NULL UNIQUE,
    status_list_index         INT                                             NOT NULL,
    status_list_reference_id  BIGINT                                          NOT NULL,
    FOREIGN KEY (status_list_reference_id) REFERENCES status_list_reference (id)
);
CREATE INDEX IF NOT EXISTS wallet_instance_reference_idx ON wallet_instance_status_reference (wallet_instance_reference);
GRANT SELECT, INSERT, UPDATE, DELETE ON wallet_instance_status_reference TO ${APP_USER};
