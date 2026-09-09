/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PidMasterTokenStatusRepository extends JpaRepository<PidMasterTokenStatusEntity, Long> {

    boolean existsByTokenIDEquals(String tokenID);

    Optional<PidMasterTokenStatusEntity> findByTokenIDEquals(String tokenID);

    List<PidMasterTokenStatusEntity> findAllByWalletInstanceRef(String walletInstanceRef);
}
