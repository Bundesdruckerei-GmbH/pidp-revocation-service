/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.service;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleAdapter;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidCredentialLifecycleAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidMasterTokenNotExistException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(OutputCaptureExtension.class)
@Isolated
class PidCredentialServiceTest {

    private final PidCredentialLifecycleAdapter adapterMock = mock(PidCredentialLifecycleAdapter.class);
    private final PidCredentialService subject = new PidCredentialService(adapterMock);

    @Test
    void shouldinitPidCredencialLifecycle() {
        CredentialInfo credentialInfo = new CredentialInfo(Instant.parse("2050-05-07T10:00:00Z"), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
        String pidMasterTokenID = UUID.randomUUID().toString();
        assertThatNoException().isThrownBy(() -> subject.initPidCredentialLifecycle(pidMasterTokenID, List.of(credentialInfo)));

        verify(adapterMock).initPidCredentialLifeCycle(pidMasterTokenID, List.of(
            new CredentialInfo(credentialInfo.expiration(), credentialInfo.uri(), credentialInfo.index())));
    }

    @Test
    void shouldinitPidCredencialLifecycleToInvalidWhenExpired() {
        CredentialInfo credentialInfo = new CredentialInfo(Instant.parse("2026-01-01T10:00:00Z"), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
        String pidMasterTokenID = UUID.randomUUID().toString();
        assertThatNoException().isThrownBy(() -> subject.initPidCredentialLifecycle(pidMasterTokenID, List.of(credentialInfo)));

        verify(adapterMock).initPidCredentialLifeCycle(pidMasterTokenID, List.of(
            new CredentialInfo(credentialInfo.expiration(), credentialInfo.uri(), credentialInfo.index())));
    }

    @Test
    void shouldThrowExceptionWhenPidMasterTokenNotFound() {
        doThrow(TokenNotFoundException.class).when(adapterMock).initPidCredentialLifeCycle(any(), any());

        CredentialInfo credentialInfo = new CredentialInfo(Instant.parse("2050-05-07T10:00:00Z"), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
        var credentialInfoList = List.of(credentialInfo);
        String pidMasterTokenID = UUID.randomUUID().toString();
        assertThatThrownBy(() -> subject.initPidCredentialLifecycle(pidMasterTokenID, credentialInfoList))
            .isInstanceOf(PidMasterTokenNotExistException.class);
    }

    @Test
    void shouldLogSecurityLogWhenPidCredentialLifecycleAlreadyExists(CapturedOutput output) {
        doThrow(PidCredentialLifecycleAlreadyExistsException.class).when(adapterMock).initPidCredentialLifeCycle(any(), any());

        CredentialInfo credentialInfo = new CredentialInfo(Instant.parse("2050-05-07T10:00:00Z"), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1);
        var credentialInfoList = List.of(credentialInfo);
        String pidMasterTokenID = UUID.randomUUID().toString();
        assertThatThrownBy(() -> subject.initPidCredentialLifecycle(pidMasterTokenID, credentialInfoList))
            .isInstanceOf(PidCredentialLifecycleAlreadyExistsException.class);

        assertThat(output.getOut()).contains("logType=security", "Lifecycle initialization for existing PID credential attempted");
    }
}
