/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletInstanceStatusRefRepository extends JpaRepository<WalletInstanceStatusRefEntity, Long> {
    List<WalletInstanceStatusRefEntity> findByWalletInstanceReference(String walletInstanceReference);
    boolean existsByWalletInstanceReference(String walletInstanceReference);
}
