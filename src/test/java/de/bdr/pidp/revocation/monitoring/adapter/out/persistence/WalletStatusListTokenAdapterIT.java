/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import de.bdr.pidp.revocation.monitoring.app.domain.StatusListNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Isolated
class WalletStatusListTokenAdapterIT {

    @Autowired
    private WalletStatusListTokenAdapter walletStatusListTokenAdapter;

    @Autowired
    private WalletInstanceStatusRefRepository walletInstanceStatusRefRepository;

    @Autowired
    private StatusListRefRepository statusListRefRepository;

    @Autowired
    private ProcessedWalletStatusListTokenRepository processedWalletStatusListTokenRepository;

    private static final String RUN_ID = "test-run-123";
    private static final String RUN_ID_2 = "test-run-456";
    private static final URI URI_1 = URI.create("https://bdr.de/test/sl1");
    private static final URI URI_2 = URI.create("https://bdr.de/test/sl2");
    private static final URI URI_3 = URI.create("https://bdr.de/test/sl3");

    @BeforeEach
    void setUp() {
        processedWalletStatusListTokenRepository.deleteAll();
        walletInstanceStatusRefRepository.deleteAll();
        statusListRefRepository.deleteAll();

        statusListRefRepository.saveAll(List.of(
                new StatusListRefEntity(URI_1),
                new StatusListRefEntity(URI_2),
                new StatusListRefEntity(URI_3)
        ));
    }

    @Test
    void shouldReturnAllUnprocessedStatusListURIsWhenNoneProcessed() {
        // When
        List<URI> unprocessed = walletStatusListTokenAdapter.getUnprocessedStatusListURIs(RUN_ID);

        // Then
        assertThat(unprocessed).containsExactlyInAnyOrder(URI_1, URI_2, URI_3);
    }

    @Test
    void shouldReturnOnlyUnprocessedStatusListURIs() {
        // Given
        var sl1 = statusListRefRepository.findByUri(URI_1).orElseThrow();
        var token = new ProcessedWalletStatusListTokenEntity(RUN_ID, sl1, new Integer[]{1, 2});
        processedWalletStatusListTokenRepository.save(token);

        // When
        List<URI> unprocessed = walletStatusListTokenAdapter.getUnprocessedStatusListURIs(RUN_ID);

        // Then
        assertThat(unprocessed).containsExactlyInAnyOrder(URI_2, URI_3).doesNotContain(URI_1);
    }

    @Test
    void shouldReturnAllURIsWhenTheyWereProcessedInDifferentRun() {
        // Given
        var sl1 = statusListRefRepository.findByUri(URI_1).orElseThrow();
        var token = new ProcessedWalletStatusListTokenEntity(RUN_ID_2, sl1, new Integer[]{1, 2});
        processedWalletStatusListTokenRepository.save(token);

        // When
        List<URI> unprocessed = walletStatusListTokenAdapter.getUnprocessedStatusListURIs(RUN_ID);

        // Then
        assertThat(unprocessed).containsExactlyInAnyOrder(URI_1, URI_2, URI_3);
    }

    @Test
    void shouldSaveProcessedStatusListToken() {
        // Given
        List<Integer> revokedIndexes = List.of(10, 20, 30);

        // When
        walletStatusListTokenAdapter.putAsProcessedStatusListToken(RUN_ID, URI_1, revokedIndexes);

        // Then
        var processedTokens = processedWalletStatusListTokenRepository.findAll();
        assertThat(processedTokens).hasSize(1);
        var token = processedTokens.getFirst();
        assertThat(token.getVerificationRunID()).isEqualTo(RUN_ID);
        assertThat(token.getStatusListRefEntity().getUri()).isEqualTo(URI_1);
        assertThat(token.getRevokedIndexes()).containsExactly(10, 20, 30);
    }

    @Test
    void shouldThrowExceptionWhenStatusListNotFound() {
        // Given
        URI nonExistingUri = URI.create("https://bdr.de/test/non-existing");

        // When / Then
        assertThatThrownBy(() -> walletStatusListTokenAdapter.putAsProcessedStatusListToken(RUN_ID, nonExistingUri, List.of()))
                .isInstanceOf(StatusListNotFoundException.class)
                .hasMessage("StatusListRef for given URI not found.");
    }
}
