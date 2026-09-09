/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
@Component
public class WalletInstanceStatusAdapter {

    private final StatusListRefRepository statusListRepo;
    private final WalletInstanceStatusRefRepository walletStatusRepo;

    public WalletInstanceStatusAdapter(
        StatusListRefRepository statusListRepo,
        WalletInstanceStatusRefRepository walletStatusRepo
    ) {
        this.statusListRepo = statusListRepo;
        this.walletStatusRepo = walletStatusRepo;
    }

    @Transactional
    public String saveStatusListRef(StatusListRef statusListRef) {
        var statusList = statusListRepo.findByUri(statusListRef.uri())
            .orElseGet(() -> {
                StatusListRefEntity newRef = new StatusListRefEntity(statusListRef.uri());
                return statusListRepo.save(newRef);
            });

        var walletRef = statusListRef.generateIdentifier();

        if (!walletStatusRepo.existsByWalletInstanceReference(walletRef)) {
            var walletStatus = new WalletInstanceStatusRefEntity(
                walletRef,
                statusListRef.index(),
                statusList
            );
            walletStatusRepo.save(walletStatus);
        }
        return walletRef;
    }
}
