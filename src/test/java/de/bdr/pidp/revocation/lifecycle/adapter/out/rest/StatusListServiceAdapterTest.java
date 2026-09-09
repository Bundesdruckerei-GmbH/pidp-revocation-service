/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.rest;

import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.api.DefaultApi;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.api.model.UpdateStatusRequest;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class StatusListServiceAdapterTest {

    @Mock
    DefaultApi statusListServiceClient;

    @InjectMocks
    StatusListServiceAdapter statusListServiceAdapter;

    @Test
    void itWorks() {
        assertThatNoException().isThrownBy(() -> statusListServiceAdapter.updateStatus(getCredentialInfos()));
    }

    @Test
    void itThrowsOnInvalidParameters() {
        doThrow(RestClientResponseException.class).when(statusListServiceClient).updateStatus(any(UpdateStatusRequest.class));
        var issuance = getCredentialInfos();
        assertThatThrownBy(() -> statusListServiceAdapter.updateStatus(issuance))
            .isInstanceOf(RestClientResponseException.class);
    }

    private List<CredentialInfo> getCredentialInfos() {
        return List.of(new CredentialInfo(Instant.parse("2050-05-07T10:00:00Z"), URI.create("https://bdr.de/statusList/" + UUID.randomUUID()), 1));
    }
}
