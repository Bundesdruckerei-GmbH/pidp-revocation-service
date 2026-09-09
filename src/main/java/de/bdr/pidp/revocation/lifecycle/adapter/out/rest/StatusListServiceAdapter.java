/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.rest;


import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.api.DefaultApi;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.api.model.UpdateStatusRequest;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatusListServiceAdapter {
    private static final Integer REVOKED_PID_STATUS_VALUE = 1;

    private final DefaultApi statusListServiceClient;

    public void updateStatus(List<CredentialInfo> credentialInfos) {
        credentialInfos.forEach(credentialInfo -> {
            log.info("Notifying status service for listId: {} index: {}", credentialInfo.uri(), credentialInfo.index());
            updateStatus(credentialInfo);
        });
    }

    private void updateStatus(CredentialInfo credentialInfo) {
        val updateStatusRequest = new UpdateStatusRequest().uri(credentialInfo.uri().toString()).index(credentialInfo.index()).value(REVOKED_PID_STATUS_VALUE);
        log.debug("update status with request: {}", updateStatusRequest);
        statusListServiceClient.updateStatus(updateStatusRequest);
        log.debug("update status successful for list {} and index {}", credentialInfo.uri(), credentialInfo.index());
    }
}
