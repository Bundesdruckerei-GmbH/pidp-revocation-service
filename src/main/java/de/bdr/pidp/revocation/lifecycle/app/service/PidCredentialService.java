/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.service;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleAdapter;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidCredentialLifecycleAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidMasterTokenNotExistException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import de.bdr.pidp.revocation.shared.LogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PidCredentialService {
    private final PidCredentialLifecycleAdapter pidCredentialLifecycleAdapter;

    public PidCredentialService(PidCredentialLifecycleAdapter pidCredentialLifecycleAdapter) {
        this.pidCredentialLifecycleAdapter = pidCredentialLifecycleAdapter;
    }

    public void initPidCredentialLifecycle(String pidMasterTokenID, List<CredentialInfo> credentialInfoList) {
        try {
            pidCredentialLifecycleAdapter.initPidCredentialLifeCycle(pidMasterTokenID, credentialInfoList);
            log.info("New list of PID Credential lifecycle initialized");
        } catch (TokenNotFoundException te) {
            throw new PidMasterTokenNotExistException(te.getMessage());
        } catch (PidCredentialLifecycleAlreadyExistsException e) {
            try (var _ = LogType.mdcContext(LogType.Value.SECURITY)) {
                log.error("Lifecycle initialization for existing PID credential attempted", e.getCause());
            }
            throw e;
        }
    }
}
