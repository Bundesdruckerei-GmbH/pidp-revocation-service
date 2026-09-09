/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PidCredentialLifecycleRepository extends JpaRepository<PidCredentialLifecycleEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE pid_credential_lifecycle SET status = CAST(:status AS lifecycle_status) WHERE pid_master_token_status_id IN :pidMasterTokenIDs", nativeQuery = true)
    void updateStatusByPidMasterTokens(@Param("pidMasterTokenIDs") List<Long> pidMasterTokenIDs, @Param("status") String status);

    List<PidCredentialLifecycleEntity> findAllByMasterTokenStatus_IdIn(List<Long> masterTokenIDs);
}
