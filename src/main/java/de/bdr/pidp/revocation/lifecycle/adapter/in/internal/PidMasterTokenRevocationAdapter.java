/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.internal;

import de.bdr.pidp.revocation.lifecycle.app.service.PidMasterTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PidMasterTokenRevocationAdapter {
    private final PidMasterTokenService tokenService;

    public void revokeAllPidMasterToken(String walletInstantReference) {
        tokenService.revokePidMasterTokens(walletInstantReference);
        log.info("All PID Master Tokens associated with the wallet instance reference {} have been revoked.", walletInstantReference);
    }
}
