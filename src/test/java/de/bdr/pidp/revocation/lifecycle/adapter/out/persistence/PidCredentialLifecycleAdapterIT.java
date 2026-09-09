/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidCredentialLifecycleAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PidCredentialLifecycleAdapterIT {

    @Autowired
    private PidCredentialLifecycleAdapter adapter;

    @Autowired
    private PidCredentialLifecycleRepository repository;

    @Autowired
    private PidMasterTokenStatusRepository pidMasterTokenStatusRepository;

    @Test
    void shouldInitLifecycle() {
        var pidMasterToken = UUID.randomUUID().toString();
        initPidMasterTokenStatus(pidMasterToken);

        var credentialInfo = createRandomCredentialInfo();
        adapter.initPidCredentialLifeCycle(pidMasterToken, List.of(credentialInfo));

        var entry = repository.findAll().stream().filter(e -> e.getListID().equals(credentialInfo.uri())).findFirst();
        assertThat(entry)
            .get()
            .extracting(PidCredentialLifecycleEntity::getListID, PidCredentialLifecycleEntity::getListIndex,
                PidCredentialLifecycleEntity::getExpirationTime, p -> p.getMasterTokenStatus().getTokenID(),
                PidCredentialLifecycleEntity::getStatus)
            .containsExactly(credentialInfo.uri(), credentialInfo.index(), credentialInfo.expiration(),
                pidMasterToken, LifecycleStatus.VALID);
    }

    @Test
    void shouldThrowExceptionWhenPidMasterTokenNotExists() {
        var pidMasterToken = UUID.randomUUID().toString();

        var credentialInfo = createRandomCredentialInfo();
        var credentialInfos = List.of(credentialInfo);

        assertThatThrownBy(() -> adapter.initPidCredentialLifeCycle(pidMasterToken, credentialInfos))
            .isInstanceOf(TokenNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionOnExistingEntry() {
        var pidMasterToken = UUID.randomUUID().toString();
        var pidMasterTokenStatusEntity= initPidMasterTokenStatus(pidMasterToken);

        var credentialInfo = createRandomCredentialInfo();
        var credentialInfos = List.of(credentialInfo);

        savePidCredentialEntity(credentialInfo, pidMasterTokenStatusEntity);

        assertThatThrownBy(() -> adapter.initPidCredentialLifeCycle(pidMasterToken, credentialInfos))
            .isInstanceOf(PidCredentialLifecycleAlreadyExistsException.class);
    }

    @Test
    void shouldFindAllByPidMasterTokenIDs() {
        var pidMasterToken = UUID.randomUUID().toString();
        var masterTokenEntity = initPidMasterTokenStatus(pidMasterToken);

        var info1 = createRandomCredentialInfo();
        var info2 = createRandomCredentialInfo();

        savePidCredentialEntity(info1, masterTokenEntity);
        savePidCredentialEntity(info2, masterTokenEntity);

        var result = adapter.findAllByPidMasterTokenIDs(List.of(masterTokenEntity.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CredentialInfo::uri)
            .containsExactlyInAnyOrder(info1.uri(), info2.uri());
    }

    @Test
    void shouldBulkUpdateStatus() {
        var pidMasterToken = UUID.randomUUID().toString();
        var masterTokenEntity = initPidMasterTokenStatus(pidMasterToken);

        var info1 = createRandomCredentialInfo();
        var info2 = createRandomCredentialInfo();
        var info3 = createRandomCredentialInfo();

        savePidCredentialEntity(info1, masterTokenEntity);
        savePidCredentialEntity(info2, masterTokenEntity);
        savePidCredentialEntity(info3, masterTokenEntity);

        adapter.bulkUpdateStatus(List.of(masterTokenEntity.getId()), LifecycleStatus.INVALID);

        var updatedEntries = repository.findAllByMasterTokenStatus_IdIn(List.of(masterTokenEntity.getId()));
        assertThat(updatedEntries).hasSize(3).allSatisfy(entity -> {
            assertThat(entity.getMasterTokenStatus()).isEqualTo(masterTokenEntity);
            assertThat(entity.getListID()).isIn(info1.uri(), info2.uri(), info3.uri());
            assertThat(entity.getStatus()).isEqualTo(LifecycleStatus.INVALID);
        });
    }

    private void savePidCredentialEntity(CredentialInfo info1, PidMasterTokenStatusEntity masterTokenEntity) {
        repository.save(new PidCredentialLifecycleEntity(info1.uri(), info1.index(), info1.expiration(), masterTokenEntity));
    }

    private static @NonNull CredentialInfo createRandomCredentialInfo() {
        return new CredentialInfo(Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MICROS), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
    }

    private PidMasterTokenStatusEntity initPidMasterTokenStatus(String pidMasterTokenStatusID) {
        var entity = new PidMasterTokenStatusEntity(pidMasterTokenStatusID, UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
        return pidMasterTokenStatusRepository.save(entity);
    }
}
