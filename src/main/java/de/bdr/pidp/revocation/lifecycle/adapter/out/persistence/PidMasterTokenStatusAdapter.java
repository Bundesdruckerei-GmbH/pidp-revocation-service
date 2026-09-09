/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import de.bdr.pidp.revocation.lifecycle.app.domain.TokenIDAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@NullMarked
@Service
public class PidMasterTokenStatusAdapter {

    private final PidMasterTokenStatusRepository repository;

    public PidMasterTokenStatusAdapter(PidMasterTokenStatusRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void initLifecycle(TokenInfo tokenInfo, @Nullable String walletInstanceStatusRef) {
        if (repository.existsByTokenIDEquals(tokenInfo.tokenID())) {
            throw new TokenIDAlreadyExistsException("Lifecycle for PID Master Token with TokenID already exists");
        }
        var token = new PidMasterTokenStatusEntity(tokenInfo.tokenID(), tokenInfo.pseudonym(), tokenInfo.expiration());
        if (walletInstanceStatusRef != null) {
            token.setWalletInstanceRef(walletInstanceStatusRef);
        }

        repository.save(token);
    }

    @Transactional
    public List<Long> updateStatus(String walletInstanceRef, LifecycleStatus newStatus) {
        var pidMasterTokenStatusEntities = repository.findAllByWalletInstanceRef(walletInstanceRef);
        pidMasterTokenStatusEntities.forEach(pmt -> pmt.setLifecycleStatus(newStatus));
        repository.saveAll(pidMasterTokenStatusEntities);
        return pidMasterTokenStatusEntities.stream().map(PidMasterTokenStatusEntity::getId).toList();
    }

    public Optional<TokenStatus> findLifecycleStatus(String tokenID) {
        return repository.findByTokenIDEquals(tokenID)
            .map(PidMasterTokenStatusEntity::getLifecycleStatus)
            .map(this::map);
    }

    private TokenStatus map(LifecycleStatus lifecycleStatus) {
        return switch (lifecycleStatus) {
            case VALID -> TokenStatus.VALID;
            case INVALID -> TokenStatus.INVALID;
        };
    }
}
