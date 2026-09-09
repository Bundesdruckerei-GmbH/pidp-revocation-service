/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import de.bdr.pidp.revocation.monitoring.app.domain.StatusList;
import de.bdr.pidp.revocation.shared.domain.RevocationServerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static de.bdr.pidp.revocation.monitoring.adapter.out.http.WalletStatusListProviderAdapter.STATUS_LIST_TOKEN_MEDIA_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = {WalletStatusListProviderAdapter.class})
class WalletStatusListProviderAdapterTest {

    private static final URI ANY_URI = URI.create("https://example.com/statuslist");

    @Autowired
    private MockRestServiceServer mockServer;

    @MockitoBean
    private StatusListTokenVerifier statusListTokenVerifier;

    @Autowired
    private WalletStatusListProviderAdapter adapter;

    @Value("classpath:statusListTokenJwt")
    private Resource resource;

    @Test
    void fetchStatusListToken_Success() throws IOException {
        // Arrange
        String jwt = readTestJwt();

        mockServer.expect(requestTo(ANY_URI))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(jwt, STATUS_LIST_TOKEN_MEDIA_TYPE));
        StatusListTokenVerifier.StatusListValues statusListValues =
            new StatusListTokenVerifier.StatusListValues(new de.bdr.pidp.revocation.shared.jwt.StatusList(1, "eNpjYEAFAAAQAAE"), 10);
        when(statusListTokenVerifier.verify(any(), any())).thenReturn(statusListValues);

        // Act
        StatusList result = adapter.fetchStatusList(ANY_URI);

        // Assert
        assertNotNull(result);
        assertEquals(new StatusList(statusListValues.statusList()), result);

        mockServer.verify();
    }

    @Test
    void fetchStatusListToken_InvalidContentType_ThrowsException() {
        // Arrange
        mockServer.expect(requestTo(ANY_URI))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("some-jwt", APPLICATION_JSON));

        // Act & Assert
        RevocationServerException exception = assertThrows(RevocationServerException.class,
            () -> adapter.fetchStatusList(ANY_URI));
        assertTrue(exception.getMessage().contains("Invalid Content-Type"));
        mockServer.verify();
    }

    @Test
    void fetchStatusListToken_EmptyBody_ThrowsException() {
        // Arrange
        mockServer.expect(requestTo(ANY_URI))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("", STATUS_LIST_TOKEN_MEDIA_TYPE));

        // Act & Assert
        RevocationServerException exception = assertThrows(RevocationServerException.class,
            () -> adapter.fetchStatusList(ANY_URI));
        assertTrue(exception.getMessage().contains("Response body is empty"));
        mockServer.verify();
    }

    @Test
    void fetchStatusListToken_InvalidJwt_ThrowsException() {
        // Arrange
        mockServer.expect(requestTo(ANY_URI))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("not-a-jwt", STATUS_LIST_TOKEN_MEDIA_TYPE));

        // Act & Assert
        RevocationServerException exception = assertThrows(RevocationServerException.class, () -> adapter.fetchStatusList(ANY_URI));
        assertTrue(exception.getMessage().startsWith("Error while fetching status list"));
        mockServer.verify();
    }

    @Test
    void fetchStatusListToken_HttpError_ThrowsException() {
        // Arrange
        mockServer.expect(requestTo(ANY_URI))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withResourceNotFound());

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> adapter.fetchStatusList(ANY_URI));
        mockServer.verify();
    }

    private String readTestJwt() throws IOException {
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
