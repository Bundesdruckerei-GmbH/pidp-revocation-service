/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jwt.proc.BadJWTException;
import de.bdr.pidp.revocation.TestCerts;
import de.bdr.pidp.revocation.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusListTokenClaimsVerifierTest {
    private final int maxClockSkew = 30;
    private final StatusListTokenClaimsVerifier verifier = new StatusListTokenClaimsVerifier(maxClockSkew);
    private final URI statusListURI = URI.create("http://bdr.de/status-list-dummy");

    private StatusListTokenSecurityContext context;

    @BeforeEach
    void setUp() {
        context = new StatusListTokenSecurityContext(
            statusListURI,
            TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET
        );
    }

    @Test
    void success() throws BadJWTException {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI).build();

        verifier.verify(claims, context);

        assertThat(context.getStatusList().bits()).isEqualTo(1);
        assertThat(context.getStatusList().lst()).isEqualTo("AAAAAAAAAAA");
        assertThat(context.getTimeToLive()).isEqualTo(300);
    }

    @Test
    void missingIssueTime() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .issueTime(null)
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Missing JWT issue time (iat) claim");
    }

    @Test
    void aheadIssueTime() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .issueTime(Date.from(Instant.now().plusSeconds(maxClockSkew * 2)))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("JWT issue time ahead of current time");
    }

    @Test
    void aheadIssueTimeWithinTolerance() throws BadJWTException {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .issueTime(Date.from(Instant.now().plusSeconds(maxClockSkew / 2)))
            .build();

        verifier.verify(claims, context);

        assertThat(context.getStatusList()).isNotNull();
    }

    @Test
    void missingExpirationTime() throws BadJWTException {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .expirationTime(null)
            .build();

        verifier.verify(claims, context);

        assertThat(context.getStatusList()).isNotNull();
    }

    @Test
    void pastExpirationTime() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .expirationTime(Date.from(Instant.now().minusSeconds(maxClockSkew * 2)))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Expired JWT");
    }

    @Test
    void pastExpirationTimeWithinTolerance() throws BadJWTException {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .issueTime(Date.from(Instant.now().minusSeconds(maxClockSkew / 2)))
            .build();

        verifier.verify(claims, context);

        assertThat(context.getStatusList()).isNotNull();
    }

    @Test
    void missingSubject() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .subject(null)
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Missing JWT subject (sub) claim");
    }

    @Test
    void emptySubject() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .subject("")
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Missing JWT subject (sub) claim");
    }

    @Test
    void invalidSubject() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .subject("invalid")
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Subject does not match status list token URI");
    }

    @Test
    void missingTimeToLive() throws BadJWTException {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("ttl", null)
            .build();

        verifier.verify(claims, context);

        assertThat(context.getTimeToLive()).isNull();
    }

    @Test
    void invalidTimeToLiveType() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("ttl", "invalid")
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid time to live (ttl): could not be parsed");
    }

    @Test
    void invalidTimeToLive() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("ttl", -100)
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Time to live (ttl) is negative");
    }

    @Test
    void missingStatusList() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", null)
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Missing JWT status list");
    }

    @Test
    void emptyStatusList() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", Collections.emptyMap())
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid status list: could not be parsed");
    }

    @Test
    void missingStatusListLst() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", Map.of("bits", 1))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid status list: could not be parsed");
    }

    @Test
    void missingStatusListBits() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", Map.of("lst", "AAAAAAAAAAA"))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid status list: could not be parsed");
    }

    @Test
    void invalidStatusListLstType() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", Map.of("lst", 0, "bits", 1))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid status list: could not be parsed");
    }

    @Test
    void invalidStatusListBitsType() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(statusListURI)
            .claim("status_list", Map.of("lst", "AAAAAAAAAAA", "bits", "10"))
            .build();

        assertThatThrownBy(() -> verifier.verify(claims, context))
            .isInstanceOf(BadJWTException.class)
            .hasMessage("Invalid status list: could not be parsed");
    }
}
