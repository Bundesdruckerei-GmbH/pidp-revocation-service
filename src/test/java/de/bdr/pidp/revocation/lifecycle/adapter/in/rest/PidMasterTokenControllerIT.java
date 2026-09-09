/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.rest;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.LifecycleStatus;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusEntity;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusRepository;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PidMasterTokenControllerIT {

    private final JsonMapper jsonMapper = new JsonMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private PidMasterTokenStatusRepository repository;

    @Value("${revocation.api-key}")
    private String apiKey;

    @Nested
    class SubmitPidMasterToken {

        @Test
        void shouldSubmitPidMasterToken() {
            var requestBody = buildRequestBody();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.CREATED.value());
        }

        @Test
        void shouldSubmitPidMasterTokenRequiredFieldsOnly() {
            var requestBody = buildRequestBodyWithoutStatusList();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.CREATED.value());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid-value"})
        void shouldFailWithMissingOrInvalidApiKey(@Nullable String invalidApiKey) {
            var requestBody = buildRequestBody();
            var headers = new HashMap<String, String>();
            if (invalidApiKey != null) {
                headers.put("X-Api-Key", invalidApiKey);
            }

            given()
                .port(port)
            .when()
                .headers(headers)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @ParameterizedTest
        @ValueSource(strings = {"2050-05-07T10:00:00", "2050-05-07", "2050-05-07T10:00:00+01:00[Europe/Berlin]", "2050-02-30T10:00:00Z", ""})
        void shouldFailWithInvalidExpiration(String invalidExpiration) {
            var requestBody = buildRequestBody();
            requestBody.put("expiration", invalidExpiration);

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @ParameterizedTest
        @ValueSource(strings = {"tokenID", "expiration", "pseudonym"})
        void shouldFailWithMissingRequiredParameter(String missingParameter) {
            var requestBody = buildRequestBody();
            requestBody.remove(missingParameter);

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void shouldFailWhenTokenIDAlreadyExists() {
            var requestBody = buildRequestBody();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.CREATED.value());

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .body(requestBody)
                .contentType("application/json")
                .post("/pid-master-token/lifecycle")
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", Matchers.equalTo("Lifecycle already exists"));
        }

        private ObjectNode buildRequestBody() {
            var body = jsonMapper.createObjectNode();
            body.put("tokenID", UUID.randomUUID().toString());
            body.put("expiration", "2050-05-07T10:00:00Z");
            body.put("pseudonym", UUID.randomUUID().toString());
            var status = body.putObject("statusListRef");
            status.put("uri", "https://bdr.de/status-list");
            status.put("index", 10);
            return body;
        }

        private ObjectNode buildRequestBodyWithoutStatusList() {
            var body = jsonMapper.createObjectNode();
            body.put("tokenID", UUID.randomUUID().toString());
            body.put("expiration", "2050-05-07T10:00:00Z");
            body.put("pseudonym", UUID.randomUUID().toString());
            return body;
        }
    }

    @Nested
    class fetchTokenStatus {

        @Test
        void shouldFetchValidTokenStatus() {
            var tokenID = initLifecycleValid();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .get("/pid-master-token/{tokenID}/lifecycle/status", tokenID)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", Matchers.equalTo("VALID"));
        }

        @Test
        void shouldFetchInvalidTokenStatus() {
            var tokenID = initLifecycleInvalid();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .get("/pid-master-token/{tokenID}/lifecycle/status", tokenID)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", Matchers.equalTo("INVALID"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid-value"})
        void shouldFailWithMissingOrInvalidApiKey(@Nullable String invalidApiKey) {
            var tokenID = UUID.randomUUID().toString();
            var headers = new HashMap<String, String>();
            if (invalidApiKey != null) {
                headers.put("X-Api-Key", invalidApiKey);
            }

            given()
                .port(port)
            .when()
                .headers(headers)
                .get("/pid-master-token/{tokenID}/lifecycle/status", tokenID)
            .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void shouldFailWhenTokenIDUnknown() {
            var tokenID = UUID.randomUUID().toString();

            given()
                .port(port)
            .when()
                .header("X-Api-Key", apiKey)
                .get("/pid-master-token/{tokenID}/lifecycle/status", tokenID)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        private String initLifecycleValid() {
            var tokenID = UUID.randomUUID().toString();
            var entity = new PidMasterTokenStatusEntity(tokenID, UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
            repository.save(entity);
            return tokenID;
        }

        private String initLifecycleInvalid() {
            var tokenID = UUID.randomUUID().toString();
            var entity = new PidMasterTokenStatusEntity(tokenID, UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
            entity.setLifecycleStatus(LifecycleStatus.INVALID);
            repository.save(entity);
            return tokenID;
        }
    }
}
