/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import de.bdr.pidp.revocation.monitoring.app.domain.StatusListNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@NullMarked
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletStatusListTokenAdapter {
    private static final Integer[] EMPTY_INT_ARRAY = new Integer[0];

    private final StatusListRefRepository statusListRepo;
    private final ProcessedWalletStatusListTokenRepository processedWalletStatusListTokenRepository;

    public List<URI> getUnprocessedStatusListURIs(String verificationRunID) {
        List<ProcessedWalletStatusListTokenEntity> verifiedStatusListTokens =
            processedWalletStatusListTokenRepository.findAllByVerificationRunID(verificationRunID);
        var ids = verifiedStatusListTokens.stream()
            .map(ProcessedWalletStatusListTokenEntity::getStatusListRefEntity)
            .map(StatusListRefEntity::getId).toList();
        List<StatusListRefEntity> toVerify = statusListRepo.findAllByIdNotIn(ids);
        return toVerify.stream().map(StatusListRefEntity::getUri).toList();
    }

    public void putAsProcessedStatusListToken(String verificationRunID, URI statusListURI, List<Integer> revokedIndexes) {
        statusListRepo.findByUri(statusListURI).ifPresentOrElse(entity -> {
            var tokenEntity = new ProcessedWalletStatusListTokenEntity(verificationRunID, entity, revokedIndexes.toArray(EMPTY_INT_ARRAY));
            processedWalletStatusListTokenRepository.save(tokenEntity);
        }, () -> {
            log.error("StatusListRef for given URI not found. {}", statusListURI);
            throw new StatusListNotFoundException("StatusListRef for given URI not found.");
        });
    }
}
