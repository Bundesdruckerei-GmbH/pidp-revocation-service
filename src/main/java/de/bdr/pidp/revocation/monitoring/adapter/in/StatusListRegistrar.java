/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.in;

import de.bdr.pidp.revocation.lifecycle.port.out.StatusListRegistrationPort;
import de.bdr.pidp.revocation.monitoring.app.service.WalletInstanceStatusService;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.springframework.stereotype.Service;

@Service
public class StatusListRegistrar implements StatusListRegistrationPort {
    private final WalletInstanceStatusService walletInstanceStatusService;

    public StatusListRegistrar(WalletInstanceStatusService walletInstanceStatusService) {
        this.walletInstanceStatusService = walletInstanceStatusService;
    }

    @Override
    public Registration register(StatusListRef walletInstanceStatusListRef) {
        var wiRef = walletInstanceStatusService.register(walletInstanceStatusListRef);

        return new Registration(wiRef);
    }
}
