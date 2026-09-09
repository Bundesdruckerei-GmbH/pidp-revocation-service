--
-- Copyright 2024-2026 Bundesdruckerei GmbH
-- For the license see the accompanying file LICENSE.MD.
--

CREATE INDEX IF NOT EXISTS pid_master_token_status_wallet_instance_reference_idx ON pid_master_token_status (wallet_instance_reference);

ALTER TABLE pid_credential_lifecycle
DROP CONSTRAINT IF EXISTS pid_credential_lifecycle_pid_master_token_status_id_fkey;

ALTER TABLE pid_credential_lifecycle
    ADD CONSTRAINT pid_credential_lifecycle_pid_master_token_status_id_fkey
        FOREIGN KEY (pid_master_token_status_id)
            REFERENCES pid_master_token_status (id)
            ON DELETE CASCADE;
