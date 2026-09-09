/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jwt.SignedJWT;
import de.bdr.pidp.revocation.monitoring.app.domain.StatusList;
import de.bdr.pidp.revocation.shared.domain.RevocationServerException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.text.ParseException;

@Service
public class WalletStatusListProviderAdapter {
    private static final String STATUS_LIST_TOKEN_TYPE = "application";
    private static final String STATUS_LIST_TOKEN_SUB_TYPE = "statuslist+jwt";
    protected static final MediaType STATUS_LIST_TOKEN_MEDIA_TYPE = new MediaType(STATUS_LIST_TOKEN_TYPE, STATUS_LIST_TOKEN_SUB_TYPE);

    private final RestClient restClient;
    private final StatusListTokenVerifier verifier;

    public WalletStatusListProviderAdapter(RestClient.Builder restClientBuilder, StatusListTokenVerifier verifier) {
        this.restClient = restClientBuilder.defaultHeader(HttpHeaders.ACCEPT, STATUS_LIST_TOKEN_MEDIA_TYPE.toString()).build();
        this.verifier = verifier;
    }
    /**
     * Retrieves the status list from the URL and verifies the SignedJWT.
     *
     * @param statusListUri the target-URI
     * @return the verified token status list
     * @throws RevocationServerException in the case of HTTP errors, incorrect content types, or signature errors
     */
    public StatusList fetchStatusList(URI statusListUri) {
        try {
            ResponseEntity<String> response = restClient.get()
                .uri(statusListUri)
                .retrieve()
                .toEntity(String.class);

            MediaType contentType = response.getHeaders().getContentType();
            if (contentType == null || !STATUS_LIST_TOKEN_TYPE.equals(contentType.getType())
                || !STATUS_LIST_TOKEN_SUB_TYPE.equals(contentType.getSubtype())) {
                throw new RevocationServerException(
                    "Invalid Content-Type: Expected '%s/%s'".formatted(STATUS_LIST_TOKEN_TYPE, STATUS_LIST_TOKEN_SUB_TYPE));
            }

            String jwtString = response.getBody();
            if (jwtString == null) {
                throw new RevocationServerException("Response body is empty");
            }

            SignedJWT signedJWT = SignedJWT.parse(jwtString);

            StatusListTokenVerifier.StatusListValues statusListValues = verifier.verify(signedJWT, statusListUri);

            return new StatusList(statusListValues.statusList());
        } catch (ParseException e) {
            throw new RevocationServerException("Error while fetching status list: " + e.getMessage());
        }
    }
}
