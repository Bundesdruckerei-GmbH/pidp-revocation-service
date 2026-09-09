/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.service;

import de.bdr.pidp.revocation.monitoring.adapter.out.persistence.WalletInstanceStatusAdapter;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.springframework.stereotype.Service;

@Service
public class WalletInstanceStatusService {

    private final WalletInstanceStatusAdapter walletInstanceStatusAdapter;

    public WalletInstanceStatusService(WalletInstanceStatusAdapter walletInstanceStatusAdapter) {
        this.walletInstanceStatusAdapter = walletInstanceStatusAdapter;
    }

    public String register(StatusListRef statusListRef) {
        return walletInstanceStatusAdapter.saveStatusListRef(statusListRef);
    }
}
