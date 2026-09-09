/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jose.JOSEObjectType;
import de.bdr.pidp.revocation.TestCerts;
import de.bdr.pidp.revocation.TestUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusListTokenVerifierTest {

    private static final URI STATUS_LIST_URI = URI.create("http://bdr.de/status-list-dummy");

    private final StatusListTokenVerifier verifier = new StatusListTokenVerifier(
        TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET,
        Duration.ofSeconds(30)
    );

    @Test
    void success() {
        var token = TestUtils.buildValidStatusListToken(STATUS_LIST_URI);

        var result = verifier.verify(token, STATUS_LIST_URI);

        assertThat(result.statusList().bits()).isOne();
        assertThat(result.statusList().lst()).isEqualTo("AAAAAAAAAAA");
        assertThat(result.timeToLive()).isEqualTo(300);
    }

    @Test
    void invalidJOSEObjectType() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(STATUS_LIST_URI).build();
        var token = TestUtils.buildStatusListToken(claims, JOSEObjectType.JWT);

        assertThatThrownBy(() -> verifier.verify(token, STATUS_LIST_URI))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Status List Token invalid: JOSE header typ (type) JWT not allowed");
    }

    @Test
    void missingX5CHeader() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(STATUS_LIST_URI).build();
        var token = TestUtils.buildX5CJWT(claims, TestUtils.STATUS_LIST_TYPE, (ECPrivateKey) TestCerts.CHAIN_LEAF_PRIV, null);

        assertThatThrownBy(() -> verifier.verify(token, STATUS_LIST_URI))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Status List Token invalid: Missing JOSE X.509 certificate chain (x5c) header");
    }

    @Test
    void invalidSignature() {
        var claims = TestUtils.getStatusListClaimsBuilderWithDefaults(STATUS_LIST_URI).build();
        var token = TestUtils.buildX5CJWT(claims, TestUtils.STATUS_LIST_TYPE, (ECPrivateKey) TestCerts.SELF_SIGNED_PRIV, List.of(TestCerts.SELF_SIGNED_B64));

        assertThatThrownBy(() -> verifier.verify(token, STATUS_LIST_URI))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Status List Token invalid: Invalid JOSE X.509 certificate chain (x5c) header: Path does not chain with any of the trust anchors");
    }

    @Test
    void invalidSubject() {
        var token = TestUtils.buildValidStatusListToken(URI.create("http://bdr.de/dummy-status-list"));

        assertThatThrownBy(() -> verifier.verify(token, STATUS_LIST_URI))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Status List Token invalid: Subject does not match status list token URI");
    }
}
