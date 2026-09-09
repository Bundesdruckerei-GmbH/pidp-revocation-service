/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface StatusListRefRepository extends JpaRepository<StatusListRefEntity, Long> {
    Optional<StatusListRefEntity> findByUri(URI uri);
    List<StatusListRefEntity> findAllByIdNotIn(List<Long> ids);
}
