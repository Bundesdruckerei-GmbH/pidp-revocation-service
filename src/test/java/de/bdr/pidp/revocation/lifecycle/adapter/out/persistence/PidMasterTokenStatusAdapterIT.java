/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import de.bdr.pidp.revocation.lifecycle.app.domain.TokenIDAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PidMasterTokenStatusAdapterIT {

    @Autowired
    private PidMasterTokenStatusAdapter adapter;

    @Autowired
    private PidMasterTokenStatusRepository repository;

    @Test
    void shouldInitLifecycle() {
        var tokenInfo = new TokenInfo(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
        var walletInstanceRef = "9a16346e6ec4412f82eeac8758474e2b3235fabfad6c664bb346e903b3371668";
        adapter.initLifecycle(tokenInfo, walletInstanceRef);

        var entry = repository.findAll().stream().filter(e -> e.getTokenID().equals(tokenInfo.tokenID())).findFirst();
        assertThat(entry)
            .get()
            .extracting(PidMasterTokenStatusEntity::getTokenID, PidMasterTokenStatusEntity::getPseudonym, PidMasterTokenStatusEntity::getExpirationTime, PidMasterTokenStatusEntity::getLifecycleStatus, PidMasterTokenStatusEntity::getWalletInstanceRef)
            .containsExactly(tokenInfo.tokenID(), tokenInfo.pseudonym(), tokenInfo.expiration(), LifecycleStatus.VALID, walletInstanceRef);
    }

    @Test
    void shouldInitLifecycleWithoutWIStatus() {
        var tokenInfo = new TokenInfo(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
        adapter.initLifecycle(tokenInfo, null);

        var entry = repository.findAll().stream().filter(e -> e.getTokenID().equals(tokenInfo.tokenID())).findFirst();
        assertThat(entry)
            .get()
            .extracting(PidMasterTokenStatusEntity::getTokenID, PidMasterTokenStatusEntity::getPseudonym, PidMasterTokenStatusEntity::getExpirationTime, PidMasterTokenStatusEntity::getLifecycleStatus, PidMasterTokenStatusEntity::getWalletInstanceRef)
            .containsExactly(tokenInfo.tokenID(), tokenInfo.pseudonym(), tokenInfo.expiration(), LifecycleStatus.VALID, null);
    }

    @Test
    void shouldThrowExceptionOnExistingEntry() {
        var tokenID = UUID.randomUUID().toString();
        var tokenInfo = new TokenInfo(tokenID, "pseudo", Instant.parse("2050-05-07T10:00:00Z"));
        var pmt = new PidMasterTokenStatusEntity(tokenID, "nym", Instant.parse("2042-05-07T10:00:00Z"));
        repository.save(pmt);

        assertThatThrownBy(() -> adapter.initLifecycle(tokenInfo, null))
            .isInstanceOf(TokenIDAlreadyExistsException.class);
    }

    @ParameterizedTest
    @EnumSource(LifecycleStatus.class)
    void shouldFindLifecycleStatus(LifecycleStatus lifecycleStatus) {
        var tokenID = UUID.randomUUID().toString();
        var pmt = new PidMasterTokenStatusEntity(tokenID, UUID.randomUUID().toString(), Instant.parse("2042-05-07T10:00:00Z"));
        pmt.setLifecycleStatus(lifecycleStatus);
        repository.save(pmt);

        var status = adapter.findLifecycleStatus(tokenID);

        var expectedStatus = switch (lifecycleStatus) {
            case VALID -> TokenStatus.VALID;
            case INVALID -> TokenStatus.INVALID;
        };
        assertThat(status).get().isEqualTo(expectedStatus);
    }

    @Test
    void shouldReturnEmptyWhenTokenIDUnknown() {
        var tokenID = UUID.randomUUID().toString();

        var status = adapter.findLifecycleStatus(tokenID);

        assertThat(status).isEmpty();
    }
}
