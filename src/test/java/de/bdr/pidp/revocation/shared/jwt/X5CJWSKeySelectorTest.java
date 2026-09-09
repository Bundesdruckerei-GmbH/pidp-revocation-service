/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.util.Base64;
import de.bdr.pidp.revocation.TestCerts;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@NullMarked
class X5CJWSKeySelectorTest {

    private final X5CJWSKeySelector<X509SecurityContext> keySelector = new X5CJWSKeySelector<>(Set.of(JWSAlgorithm.ES256), true);
    private final X509SecurityContext context = () -> TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET;

    @Test
    void success() throws KeySourceException {
        var header = buildJWSHeader(JWSAlgorithm.ES256, TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR_B64);

        var keys = keySelector.selectJWSKeys(header, context);

        var expectedPubKeys = Collections.singletonList(TestCerts.CHAIN_LEAF_PUB.getPublicKey());
        assertThat(keys).isEqualTo(expectedPubKeys);
    }

    @Test
    void unsupportedAlg() throws KeySourceException {
        var header = buildJWSHeader(JWSAlgorithm.PS256, TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR_B64);

        var keys = keySelector.selectJWSKeys(header, context);

        assertThat(keys).isEmpty();
    }

    @Test
    void missingX5CHeader() {
        var header = buildJWSHeader(JWSAlgorithm.ES256, null);

        assertThatThrownBy(() -> keySelector.selectJWSKeys(header, context))
                .isInstanceOf(KeySourceException.class)
                .hasMessage("Missing JOSE X.509 certificate chain (x5c) header");
    }

    @Test
    void unknownCert() throws CertPathValidatorException, CertificateException {
        var x509Validator = mock(X509Validator.class);
        ReflectionTestUtils.setField(keySelector, "x509Validator", x509Validator);
        doThrow(new CertPathValidatorException()).when(x509Validator).validate(any(), any());

        var header = buildJWSHeader(JWSAlgorithm.ES256, Collections.singletonList(TestCerts.UNKNOWN_LEAF_B64));

        assertThatThrownBy(() -> keySelector.selectJWSKeys(header, context))
                .isInstanceOf(KeySourceException.class)
                .hasMessageStartingWith("Invalid JOSE X.509 certificate chain (x5c) header: ");
    }

    private JWSHeader buildJWSHeader(JWSAlgorithm algorithm, @Nullable List<Base64> b64Certificates) {
        return new JWSHeader(algorithm, null, null, null, null, null, null, null, null, b64Certificates, null, true, null, null);
    }
}
