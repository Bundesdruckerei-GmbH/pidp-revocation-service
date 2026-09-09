/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessedWalletStatusListTokenRepository extends JpaRepository<ProcessedWalletStatusListTokenEntity, Long> {
    List<ProcessedWalletStatusListTokenEntity> findAllByVerificationRunID(String verificationRunID);
}
