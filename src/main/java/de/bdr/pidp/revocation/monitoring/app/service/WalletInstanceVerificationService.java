/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.service;

import de.bdr.pidp.revocation.monitoring.adapter.out.http.WalletStatusListProviderAdapter;
import de.bdr.pidp.revocation.monitoring.adapter.out.persistence.WalletStatusListTokenAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletInstanceVerificationService {

    private final WalletStatusListTokenAdapter walletStatusListTokenAdapter;
    private final WalletStatusListProviderAdapter walletStatusListProviderAdapter;

    public void verifyStatusLists(String verificationRunID) {
        List<URI> unprocessedStatusListURIs = walletStatusListTokenAdapter.getUnprocessedStatusListURIs(verificationRunID);
        unprocessedStatusListURIs.forEach(statusListURI -> {
            var statusList = walletStatusListProviderAdapter.fetchStatusList(statusListURI);
            walletStatusListTokenAdapter.putAsProcessedStatusListToken(verificationRunID, statusListURI, statusList.getRevokedIndexes());
        });
    }
}
