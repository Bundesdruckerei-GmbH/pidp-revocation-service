--
-- Copyright 2024-2026 Bundesdruckerei GmbH
-- For the license see the accompanying file LICENSE.MD.
--

CREATE TABLE IF NOT EXISTS processed_wallet_status_list_token
(
    id                        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    verification_run_id       VARCHAR                                         NOT NULL,
    status_list_reference_id  BIGINT                                          NOT NULL,
    revoked_indexes           INT[],
    FOREIGN KEY (status_list_reference_id) REFERENCES status_list_reference (id)
);
CREATE INDEX IF NOT EXISTS processed_wallet_status_list_token_verification_run_id_idx
    ON processed_wallet_status_list_token(verification_run_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON processed_wallet_status_list_token TO ${APP_USER};
