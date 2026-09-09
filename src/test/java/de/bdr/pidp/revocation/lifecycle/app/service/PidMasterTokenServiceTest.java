/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.service;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidCredentialLifecycleAdapter;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusAdapter;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.StatusListServiceAdapter;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenIDAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.port.out.StatusListRegistrationPort;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(OutputCaptureExtension.class)
@Isolated
class PidMasterTokenServiceTest {

    private final PidMasterTokenStatusAdapter adapterMock = mock(PidMasterTokenStatusAdapter.class);
    private final PidCredentialLifecycleAdapter pidCredentialLifecycleAdapterMock = mock(PidCredentialLifecycleAdapter.class);
    private final StatusListRegistrationPort statLRegistrationMock = mock(StatusListRegistrationPort.class);
    private final StatusListServiceAdapter statusListServiceAdapterMock = mock(StatusListServiceAdapter.class);
    private final PidMasterTokenService subject = new PidMasterTokenService(adapterMock, pidCredentialLifecycleAdapterMock, statLRegistrationMock, statusListServiceAdapterMock);

    @Test
    void shouldInitLifecycleWithoutWalletInstStatListRef() {
        var info = new TokenInfo("unique", "pseudo", Instant.parse("2050-05-07T10:00:00Z"));
        assertThatNoException().isThrownBy(() -> subject.initLifecycle(info, null));
    }

    @Test
    void shouldInitLifecycle() {
        var info = new TokenInfo("unique", "pseudo", Instant.parse("2050-05-07T10:00:00Z"));
        var statL = new StatusListRef(URI.create("https://bdr.de/status-list"), 1);
        assertThatNoException().isThrownBy(() -> subject.initLifecycle(info, statL));
    }

    @Test
    void shouldLogSecurityLogWhenTokenIDAlreadyExists(CapturedOutput output) {
        doThrow(TokenIDAlreadyExistsException.class).when(adapterMock).initLifecycle(any(), any());

        var info = new TokenInfo("duplicate", "pseudo", Instant.parse("2050-05-07T10:00:00Z"));
        var statL = new StatusListRef(URI.create("https://bdr.de/status-list"), 1);
        assertThatThrownBy(() -> subject.initLifecycle(info, statL))
            .isInstanceOf(TokenIDAlreadyExistsException.class);

        assertThat(output.getOut()).contains("logType=security", "Lifecycle initialization for existing token ID attempted");
    }
}
