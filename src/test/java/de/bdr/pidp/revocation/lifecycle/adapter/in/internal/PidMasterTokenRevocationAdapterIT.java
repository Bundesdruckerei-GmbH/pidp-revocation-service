/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.internal;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.LifecycleStatus;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleEntity;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleRepository;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusEntity;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusRepository;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.StatusListServiceAdapter;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenStatus;
import de.bdr.pidp.revocation.lifecycle.app.service.PidCredentialService;
import de.bdr.pidp.revocation.lifecycle.app.service.PidMasterTokenService;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class PidMasterTokenRevocationAdapterIT {

    @Autowired
    private PidMasterTokenRevocationAdapter revocationAdapter;

    @Autowired
    private PidMasterTokenService tokenService;

    @Autowired
    private PidCredentialService credentialService;

    @Autowired
    private PidCredentialLifecycleRepository pidCredentialLifecycleRepository;

    @Autowired
    private PidMasterTokenStatusRepository pidMasterTokenStatusRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private StatusListServiceAdapter statusListServiceAdapter;

    @Test
    void shouldRevokeAllPidMasterTokenAndAllAssociatedEntities() {
        // Given
        StatusListRef statusListRef = new StatusListRef(URI.create("https://extern.bdr.de/statuslist/" + UUID.randomUUID()), 1);
        StatusListRef statusListRefOther = new StatusListRef(URI.create("https://extern.bdr.de/statuslist/" + UUID.randomUUID()), 1);
        String walletInstanceRefIdentifier = statusListRef.generateIdentifier();

        String pidMasterTokenId1 = "token-id-1-" + UUID.randomUUID();
        String pidMasterTokenId2 = "token-id-2-" + UUID.randomUUID();
        String pidMasterTokenIdOther = "token-id-Other-" + UUID.randomUUID();

        var token1 = new TokenInfo(pidMasterTokenId1, "pseudonym-1", Instant.now().plusSeconds(3600));
        var token2 = new TokenInfo(pidMasterTokenId2, "pseudonym-2", Instant.now().plusSeconds(3600));
        var tokenOther = new TokenInfo(pidMasterTokenIdOther, "pseudonym-Other", Instant.now().plusSeconds(3600));

        tokenService.initLifecycle(token1, statusListRef);
        tokenService.initLifecycle(token2, statusListRef);
        tokenService.initLifecycle(tokenOther, statusListRefOther);

        CredentialInfo credentialInfo11 = createRandomCredentialInfo();
        CredentialInfo credentialInfo12 = createRandomCredentialInfo();
        CredentialInfo credentialInfo21 = createRandomCredentialInfo();
        CredentialInfo credentialInfo22 = createRandomCredentialInfo();
        CredentialInfo credentialInfoOther = createRandomCredentialInfo();

        credentialService.initPidCredentialLifecycle(pidMasterTokenId1, List.of(credentialInfo11, credentialInfo12));
        credentialService.initPidCredentialLifecycle(pidMasterTokenId2, List.of(credentialInfo21, credentialInfo22));
        credentialService.initPidCredentialLifecycle(pidMasterTokenIdOther, List.of(credentialInfoOther));

        // When
        revocationAdapter.revokeAllPidMasterToken(walletInstanceRefIdentifier);

        // IMPORTANT: Clear JPA cache so that the native query results are loaded
        entityManager.clear();

        // Then
        // 1. Check Master Tokens are INVALID
        assertThat(tokenService.getLifecycleStatus(pidMasterTokenId1))
                .as("Master Token 1 should be INVALID")
                .isEqualTo(TokenStatus.INVALID);

        assertThat(tokenService.getLifecycleStatus(pidMasterTokenId2))
                .as("Master Token 2 should be INVALID")
                .isEqualTo(TokenStatus.INVALID);

        // 2. Check associated PidCredentialLifecycles are also INVALID
        List<Long> pidMasterTokenStatusEntityIDs = pidMasterTokenStatusRepository.findAllByWalletInstanceRef(walletInstanceRefIdentifier).stream().map(PidMasterTokenStatusEntity::getId).toList();

        List<PidCredentialLifecycleEntity> allByMasterTokenStatusIdIn = pidCredentialLifecycleRepository.findAllByMasterTokenStatus_IdIn(pidMasterTokenStatusEntityIDs);

        assertThat(allByMasterTokenStatusIdIn)
                .as("There should be associated credentials to check")
                .hasSize(4)
                .allSatisfy(credential ->
                        assertThat(credential.getStatus())
                                .as("Associated credential should have been revoked to INVALID")
                                .isEqualTo(LifecycleStatus.INVALID)
                );

        verify(statusListServiceAdapter).updateStatus(List.of(credentialInfo11, credentialInfo12, credentialInfo21, credentialInfo22));
    }

    private static @NonNull CredentialInfo createRandomCredentialInfo() {
        return new CredentialInfo(Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MICROS), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
    }

}
