/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.service;

import de.bdr.pidp.revocation.monitoring.adapter.out.http.WalletStatusListProviderAdapter;
import de.bdr.pidp.revocation.monitoring.adapter.out.persistence.WalletStatusListTokenAdapter;
import de.bdr.pidp.revocation.monitoring.app.domain.StatusList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletInstanceVerificationServiceTest {

    @Mock
    private WalletStatusListTokenAdapter walletStatusListTokenAdapter;

    @Mock
    private WalletStatusListProviderAdapter walletStatusListProviderAdapter;

    @InjectMocks
    private WalletInstanceVerificationService walletInstanceVerificationService;

    private final String verificationRunID = "test-run-123";
    private final URI statusListUri1 = URI.create("https://example.com/sl1");
    private final URI statusListUri2 = URI.create("https://example.com/sl2");

    @Test
    void verifyStatusLists_shouldProcessAllUnprocessedURIs() {
        // Given
        List<URI> unprocessedUris = List.of(statusListUri1, statusListUri2);
        StatusList token1 = mock(StatusList.class);
        StatusList token2 = mock(StatusList.class);
        List<Integer> revokedIndexes1 = List.of(1, 2);
        List<Integer> revokedIndexes2 = List.of(3, 4);

        when(walletStatusListTokenAdapter.getUnprocessedStatusListURIs(verificationRunID)).thenReturn(unprocessedUris);
        when(walletStatusListProviderAdapter.fetchStatusList(statusListUri1)).thenReturn(token1);
        when(walletStatusListProviderAdapter.fetchStatusList(statusListUri2)).thenReturn(token2);
        when(token1.getRevokedIndexes()).thenReturn(revokedIndexes1);
        when(token2.getRevokedIndexes()).thenReturn(revokedIndexes2);

        // When
        walletInstanceVerificationService.verifyStatusLists(verificationRunID);

        // Then
        verify(walletStatusListTokenAdapter).getUnprocessedStatusListURIs(verificationRunID);
        verify(walletStatusListProviderAdapter).fetchStatusList(statusListUri1);
        verify(walletStatusListProviderAdapter).fetchStatusList(statusListUri2);
        verify(walletStatusListTokenAdapter).putAsProcessedStatusListToken(verificationRunID, statusListUri1, revokedIndexes1);
        verify(walletStatusListTokenAdapter).putAsProcessedStatusListToken(verificationRunID, statusListUri2, revokedIndexes2);
    }

    @Test
    void verifyStatusLists_shouldDoNothingWhenNoURIs() {
        // Given
        when(walletStatusListTokenAdapter.getUnprocessedStatusListURIs(verificationRunID)).thenReturn(List.of());

        // When
        walletInstanceVerificationService.verifyStatusLists(verificationRunID);

        // Then
        verify(walletStatusListTokenAdapter).getUnprocessedStatusListURIs(verificationRunID);
        verifyNoInteractions(walletStatusListProviderAdapter);
        verify(walletStatusListTokenAdapter, times(0)).putAsProcessedStatusListToken(any(), any(), any());
    }
}
