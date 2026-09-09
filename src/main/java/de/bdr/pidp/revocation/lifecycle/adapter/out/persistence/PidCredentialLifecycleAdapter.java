/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidCredentialLifecycleAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@NullMarked
@Service
public class PidCredentialLifecycleAdapter {

    private final PidCredentialLifecycleRepository credentialRepo;
    private final PidMasterTokenStatusRepository masterTokenRepo;

    public PidCredentialLifecycleAdapter(PidCredentialLifecycleRepository credentialRepo,
                                         PidMasterTokenStatusRepository masterTokenRepo) {
        this.credentialRepo = credentialRepo;
        this.masterTokenRepo = masterTokenRepo;
    }

    @Transactional
    public void initPidCredentialLifeCycle(String masterTokenId, List<CredentialInfo> credentialInfoList) {
        var masterEntity = masterTokenRepo.findByTokenIDEquals(masterTokenId)
            .orElseThrow(() -> new TokenNotFoundException("PID Master Token not found: " + masterTokenId));

        List<PidCredentialLifecycleEntity> pidCredentialLifecycleEntities =
            credentialInfoList.stream().map(credentialLifecycle -> new PidCredentialLifecycleEntity(
                credentialLifecycle.uri(),
                credentialLifecycle.index(),
                credentialLifecycle.expiration(),
                masterEntity)
            ).toList();

        try {
            credentialRepo.saveAll(pidCredentialLifecycleEntities);
        } catch (DataIntegrityViolationException e) {
            throw new PidCredentialLifecycleAlreadyExistsException("Lifecycle for PID Credential with token status list reference already exists", e);
        }
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> pidMasterTokenIDs, LifecycleStatus status) {
        credentialRepo.updateStatusByPidMasterTokens(pidMasterTokenIDs, status.name());
    }

    public List<CredentialInfo> findAllByPidMasterTokenIDs(List<Long> pidMasterTokenIDs) {
        return credentialRepo.findAllByMasterTokenStatus_IdIn(pidMasterTokenIDs).stream()
            .map(entity -> new CredentialInfo(
                entity.getExpirationTime(),
                entity.getListID(),
                entity.getListIndex()))
            .toList();
    }
}
