/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Isolated
class WalletInstanceStatusAdapterIT {

    @Autowired
    private WalletInstanceStatusAdapter walletInstanceStatusAdapter;

    @Autowired
    private StatusListRefRepository statusListRefRepository;

    @Autowired
    private WalletInstanceStatusRefRepository walletInstanceStatusRefRepository;

    private long initStatListCount;
    private long initWIStatCount;

    @BeforeEach
    void setUp() {
        initStatListCount = statusListRefRepository.count();
        initWIStatCount = walletInstanceStatusRefRepository.count();
    }

    @Test
    void shouldCreateNewStatusListReferenceOnSave() {
        // Given
        var walletInstanceReference = "81a01e456639120f1b0ea75fa2ee3989bb7400e5f171195ced3537f2485c15bb";
        URI uri = URI.create("https://bdr.de/test/WalletInstanceStatusAdapter");
        int index = 1;
        var statusListRef = new StatusListRef(uri, index);

        // When
        var walletInstanceRefResult = walletInstanceStatusAdapter.saveStatusListRef(statusListRef);

        // Then
        assertThat(walletInstanceRefResult).isEqualTo(walletInstanceReference);
        assertThat(statusListRefRepository.count()).isEqualTo(initStatListCount + 1);
        assertThat(walletInstanceStatusRefRepository.count()).isEqualTo(initWIStatCount + 1);

        var savedSLRef = statusListRefRepository.findByUri(uri);
        assertThat(savedSLRef).isPresent();

        var savedWISRef = walletInstanceStatusRefRepository.findByWalletInstanceReference(walletInstanceReference).getFirst();
        assertThat(savedWISRef.getStatusListIndex()).isEqualTo(index);
        assertThat(savedWISRef.getStatusListRefEntity()).isEqualTo(savedSLRef.get());
    }

    @Test
    void shouldReuseExistingStatusListReferenceOnSave() {
        // Given
        var walletInstanceReference1 = "81a01e456639120f1b0ea75fa2ee3989bb7400e5f171195ced3537f2485c15bb";
        var walletInstanceReference2 = "f62bd562b35d6e748cab5c77133290942fae03d98838dce0b3be821a28365024";
        URI uri = URI.create("https://bdr.de/test/WalletInstanceStatusAdapter");
        int index1 = 1;
        int index2 = 2;
        var statusListRef1 = new StatusListRef(uri, index1);
        var statusListRef2 = new StatusListRef(uri, index2);

        // When
        var walletInstanceRefResult1 = walletInstanceStatusAdapter.saveStatusListRef(statusListRef1);
        var walletInstanceRefResult2 = walletInstanceStatusAdapter.saveStatusListRef(statusListRef2);

        // Then
        assertThat(walletInstanceRefResult1).isEqualTo(walletInstanceReference1);
        assertThat(walletInstanceRefResult2).isEqualTo(walletInstanceReference2);
        assertThat(statusListRefRepository.count()).isEqualTo(initStatListCount + 1);
        assertThat(walletInstanceStatusRefRepository.count()).isEqualTo(initWIStatCount + 2);

        var ref1 = walletInstanceStatusRefRepository.findByWalletInstanceReference(walletInstanceReference1).getFirst();
        var ref2 = walletInstanceStatusRefRepository.findByWalletInstanceReference(walletInstanceReference2).getFirst();

        assertThat(ref1.getStatusListRefEntity().getUri()).isEqualTo(uri);
        assertThat(ref2.getStatusListRefEntity().getUri()).isEqualTo(uri);
        assertThat(ref1.getStatusListIndex()).isEqualTo(index1);
        assertThat(ref2.getStatusListIndex()).isEqualTo(index2);
    }

    @Test
    void shouldNotUpdateExistingReferenceOnSave() {
        // Given
        var walletInstanceReference = "81a01e456639120f1b0ea75fa2ee3989bb7400e5f171195ced3537f2485c15bb";
        URI uri = URI.create("https://bdr.de/test/WalletInstanceStatusAdapter");
        int index = 1;
        var statusListRef1 = new StatusListRef(uri, index);
        var statusListRef2 = new StatusListRef(uri, index);

        // When - save first reference
        var walletInstanceRefResult1 = walletInstanceStatusAdapter.saveStatusListRef(statusListRef1);

        // Then - verify first save
        assertThat(walletInstanceRefResult1).isEqualTo(walletInstanceReference);
        long firstSaveStatListCount = statusListRefRepository.count();
        long firstSaveWIStatCount = walletInstanceStatusRefRepository.count();
        assertThat(firstSaveStatListCount).isEqualTo(initStatListCount + 1);
        assertThat(firstSaveWIStatCount).isEqualTo(initWIStatCount + 1);

        var firstReference = walletInstanceStatusRefRepository.findByWalletInstanceReference(walletInstanceRefResult1).getFirst();
        assertThat(firstReference.getStatusListIndex()).isEqualTo(index);

        // When - save second reference with same URI and index
        var walletInstanceRefResult2 = walletInstanceStatusAdapter.saveStatusListRef(statusListRef2);

        // Then - verify no update occurred
        assertThat(walletInstanceRefResult2).isEqualTo(walletInstanceReference);
        assertThat(statusListRefRepository.count()).isEqualTo(firstSaveStatListCount);
        assertThat(walletInstanceStatusRefRepository.count()).isEqualTo(firstSaveWIStatCount);

        var existingReference = walletInstanceStatusRefRepository.findByWalletInstanceReference(walletInstanceRefResult2).getFirst();
        assertThat(existingReference.getWalletInstanceReference()).isEqualTo(walletInstanceReference);
        assertThat(existingReference.getStatusListIndex()).isEqualTo(index);
    }
}
