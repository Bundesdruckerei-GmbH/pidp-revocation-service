/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.rest;

import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusEntity;
import de.bdr.pidp.revocation.lifecycle.adapter.out.persistence.PidMasterTokenStatusRepository;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PidCredentialControllerIT {

    private final JsonMapper jsonMapper = new JsonMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private PidMasterTokenStatusRepository pidMasterTokenStatusRepository;

    @Value("${revocation.api-key}")
    private String apiKey;

    @Test
    void shouldSubmitPidCredentialList() {
        String pidMasterTokenStatusID = UUID.randomUUID().toString();
        initPidMasterTokenStatus(pidMasterTokenStatusID);
        var requestBody = buildRequestBody(pidMasterTokenStatusID);

        given()
            .port(port)
            .when()
            .header("X-Api-Key", apiKey)
            .body(requestBody)
            .contentType("application/json")
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid-value"})
    void shouldFailWithMissingOrInvalidApiKey(@Nullable String invalidApiKey) {
        String pidMasterTokenStatusID = UUID.randomUUID().toString();
        initPidMasterTokenStatus(pidMasterTokenStatusID);
        var requestBody = buildRequestBody(pidMasterTokenStatusID);
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
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private static Stream<Arguments> provideInvalidRequestBodies() {
        return Stream.of(
            arguments("pidMasterTokenID is missing", (Consumer<ObjectNode>) body ->
                body.remove("pidMasterTokenID")),

            arguments("credentialList is missing", (Consumer<ObjectNode>) body ->
                body.remove("credentialList")),

            arguments("uri in tokenStatusListRef is missing", (Consumer<ObjectNode>) body -> {
                var list = (ArrayNode) body.get("credentialList");
                var item = (ObjectNode) list.get(0);
                var ref = (ObjectNode) item.get("tokenStatusListRef");
                ref.remove("uri");
            }),

            arguments("index in tokenStatusListRef is missing", (Consumer<ObjectNode>) body -> {
                var list = (ArrayNode) body.get("credentialList");
                var item = (ObjectNode) list.get(0);
                var ref = (ObjectNode) item.get("tokenStatusListRef");
                ref.remove("index");
            }),

            arguments("expiration is missing", (Consumer<ObjectNode>) body -> {
                var list = (ArrayNode) body.get("credentialList");
                var item = (ObjectNode) list.get(0);
                item.remove("expiration");
            })
        );
    }

    @ParameterizedTest(name = "Should return 400 when {0}")
    @MethodSource("provideInvalidRequestBodies")
    void shouldFailWithMissingRequiredParameter(String description, Consumer<ObjectNode> modifier) {
        String pidMasterTokenStatusID = UUID.randomUUID().toString();
        initPidMasterTokenStatus(pidMasterTokenStatusID);
        var requestBody = buildRequestBody(pidMasterTokenStatusID);

        modifier.accept(requestBody);

        given()
            .port(port)
            .when()
            .header("X-Api-Key", apiKey)
            .body(requestBody)
            .contentType("application/json")
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldFailWhenPidMasterTokenIsMissing() {
        var requestBody = buildRequestBody(UUID.randomUUID().toString());

        given()
            .port(port)
            .when()
            .header("X-Api-Key", apiKey)
            .body(requestBody)
            .contentType("application/json")
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldFailWhenPidCredentialLifecycleAlreadyExists() {
        String pidMasterTokenStatusID = UUID.randomUUID().toString();
        initPidMasterTokenStatus(pidMasterTokenStatusID);

        String tokenStatusListUri = "https://bdr.de/status-list/" + UUID.randomUUID();
        var requestBody = buildRequestBody(pidMasterTokenStatusID, tokenStatusListUri);

        given()
            .port(port)
            .when()
            .header("X-Api-Key", apiKey)
            .body(requestBody)
            .contentType("application/json")
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        given()
            .port(port)
            .when()
            .header("X-Api-Key", apiKey)
            .body(requestBody)
            .contentType("application/json")
            .post("/pid-credential/lifecycle")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private ObjectNode buildRequestBody(String pidMasterTokenStatusID) {
        return buildRequestBody(pidMasterTokenStatusID, "https://bdr.de/status-list/" + UUID.randomUUID());
    }

    private ObjectNode buildRequestBody(String pidMasterTokenStatusID, String statusListUri) {
        var body = jsonMapper.createObjectNode();
        body.put("pidMasterTokenID", pidMasterTokenStatusID);

        var credentialList = body.putArray("credentialList");

        var credentialOne = credentialList.addObject();
        credentialOne.put("expiration", "2026-12-31T23:59:59Z");
        var statusOne = credentialOne.putObject("tokenStatusListRef");
        statusOne.put("uri", statusListUri);
        statusOne.put("index", 10);
        var credentialTwo = credentialList.addObject();
        credentialTwo.put("expiration", "2026-12-31T23:59:59Z");
        var statusTwo = credentialTwo.putObject("tokenStatusListRef");
        statusTwo.put("uri", statusListUri);
        statusTwo.put("index", 20);

        return body;
    }

    private void initPidMasterTokenStatus(String pidMasterTokenStatusID) {
        var entity = new PidMasterTokenStatusEntity(pidMasterTokenStatusID, UUID.randomUUID().toString(), Instant.parse("2050-05-07T10:00:00Z"));
        pidMasterTokenStatusRepository.save(entity);
    }
}
