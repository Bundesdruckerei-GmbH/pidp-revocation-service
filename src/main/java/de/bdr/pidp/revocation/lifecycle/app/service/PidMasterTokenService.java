/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.service;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.LifecycleStatus;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleAdapter;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusAdapter;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.StatusListServiceAdapter;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenIDAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenStatus;
import de.bdr.pidp.revocation.lifecycle.port.out.StatusListRegistrationPort;
import de.bdr.pidp.revocation.shared.LogType;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PidMasterTokenService {

    private final PidMasterTokenStatusAdapter adapter;
    private final PidCredentialLifecycleAdapter lifecycleAdapter;
    private final StatusListRegistrationPort statusListRegistrar;
    private final StatusListServiceAdapter statusListServiceAdapter;

    public void initLifecycle(TokenInfo tokenInfo, @Nullable StatusListRef walletInstStatListRef) {
        var registration = walletInstStatListRef != null ? statusListRegistrar.register(walletInstStatListRef) : null;

        try {
            adapter.initLifecycle(tokenInfo, registration != null ? registration.walletInstanceRef() : null);
            log.info("New PID Master Token lifecycle initialized");
        } catch (TokenIDAlreadyExistsException e) {
            try (var _ = LogType.mdcContext(LogType.Value.SECURITY)) {
                log.error("Lifecycle initialization for existing token ID attempted");
            }
            throw e;
        }
    }

    public TokenStatus getLifecycleStatus(String tokenID) {
        var status = adapter.findLifecycleStatus(tokenID)
            .orElseThrow(() -> new TokenNotFoundException("PID Master Token lifecycle not found for the provided tokenID"));
        log.info("PID Master Token lifecycle status found: {}", status);
        return status;
    }

   @Transactional
    public void revokePidMasterTokens(String walletInstanceReference) {
        log.info("Revoking PID Master Tokens with wallet instance reference: {}", walletInstanceReference);

        // 1. Set Master Token status to INVALID and retrieve IDs
        List<Long> masterTokenIDList = adapter.updateStatus(walletInstanceReference, LifecycleStatus.INVALID);

        // 2. Performant bulk update of all associated lifecycles in the database
        lifecycleAdapter.bulkUpdateStatus(masterTokenIDList, LifecycleStatus.INVALID);

        // 3. Load all affected lifecycles for the status service
        var affectedPidCredentials = lifecycleAdapter.findAllByPidMasterTokenIDs(masterTokenIDList);

        // 4. Inform status service
        statusListServiceAdapter.updateStatus(affectedPidCredentials);
    }
}
