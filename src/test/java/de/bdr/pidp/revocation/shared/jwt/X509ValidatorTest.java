/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.jwt;

import de.bdr.pidp.revocation.TestCerts;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXReason;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
@Isolated
class X509ValidatorTest {

    private final X509Validator validator = new X509Validator(true);
    private final X509Validator validatorNoSelfSigned = new X509Validator(false);

    @Test
    void validateSelfSignedCertificate() {
        var anchorCert = TestCerts.SELF_SIGNED_SINGLETON_SET;

        assertThatNoException()
            .isThrownBy(() -> validator.validate(anchorCert, List.of(TestCerts.SELF_SIGNED)));
    }

    @Test
    void validateSelfSignedCertificateWhenSelfSignedNotAllowed(CapturedOutput output) {
        var anchorCert = TestCerts.SELF_SIGNED_SINGLETON_SET;

        assertThatThrownBy(() -> validatorNoSelfSigned.validate(anchorCert, List.of(TestCerts.SELF_SIGNED)))
            .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
            .extracting(CertPathValidatorException::getReason).isEqualTo(X509Validator.ValidationExceptionReason.SELF_SIGNED);
        assertThat(output.getOut()).contains("logType=security", "untrusted certificate, reason: SELF_SIGNED");
    }

    @Test
    void validateSelfSignedCertificateWithListOfAnchorCerts() {
        var anchorCerts = Set.of(TestCerts.SELF_SIGNED, TestCerts.CHAIN_TRUST_ANCHOR);

        assertThatNoException()
            .isThrownBy(() -> validator.validate(anchorCerts, List.of(TestCerts.SELF_SIGNED)));
    }

    @Test
    void validateSelfSignedCertificateWithListOfAnchorCertsWhenSelfSignedNotAllowed(CapturedOutput output) {
        var anchorCerts = Set.of(TestCerts.SELF_SIGNED, TestCerts.CHAIN_TRUST_ANCHOR);

        assertThatThrownBy(() -> validatorNoSelfSigned.validate(anchorCerts, List.of(TestCerts.SELF_SIGNED)))
            .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
            .extracting(CertPathValidatorException::getReason).isEqualTo(X509Validator.ValidationExceptionReason.SELF_SIGNED);
        assertThat(output.getOut()).contains("logType=security", "untrusted certificate, reason: SELF_SIGNED");
    }

    @Test
    void validateCertificateChainContainsTrustAnchor(CapturedOutput output) {
        var anchorCert = TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET;
        var fullChain = List.of(TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR.getFirst(), TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR.getLast(), TestCerts.CHAIN_TRUST_ANCHOR);

        assertThatThrownBy(() -> validatorNoSelfSigned.validate(anchorCert, fullChain))
            .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
            .extracting(CertPathValidatorException::getReason).isEqualTo(X509Validator.ValidationExceptionReason.CHAIN_CONTAINS_TRUST_ANCHOR);
        assertThat(output.getOut()).contains("logType=security", "invalid certificate, reason: CHAIN_CONTAINS_TRUST_ANCHOR");
    }

    @Test
    void validateChain() {
        var anchorCert = TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET;

        assertThatNoException()
                .isThrownBy(() -> validator.validate(anchorCert, TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR));
    }

    @Test
    void validateChainWithListOfAnchorCerts() {
        var anchorCerts = Set.of(TestCerts.SELF_SIGNED, TestCerts.CHAIN_TRUST_ANCHOR);

        assertThatNoException()
            .isThrownBy(() -> validator.validate(anchorCerts, TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR));
    }

    @Test
    void validateSubCa() {
        var anchorCert = TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET;
        var subCa = List.of(TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR.getLast());

        assertThatNoException()
                .isThrownBy(() -> validator.validate(anchorCert, subCa));
    }

    @Test
    void validateNoAnchor(CapturedOutput output) {
        var anchorCert = TestCerts.CHAIN_TRUST_ANCHOR_SINGLETON_SET;

        assertThatThrownBy(() -> validator.validate(anchorCert, List.of(TestCerts.UNKNOWN_LEAF)))
                .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
                .extracting(CertPathValidatorException::getReason).isEqualTo(PKIXReason.NO_TRUST_ANCHOR);
        assertThat(output.getOut()).contains("logType=security", "untrusted certificate, reason: NO_TRUST_ANCHOR");
    }

    @Test
    void validateCertificateExpired(CapturedOutput output) {
        var anchorCert = Set.of(TestCerts.EXPIRED_TRUST_ANCHOR);

        assertThatThrownBy(() -> validator.validate(anchorCert, List.of(TestCerts.EXPIRED_LEAF)))
                .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
                .extracting(CertPathValidatorException::getReason).isEqualTo(CertPathValidatorException.BasicReason.EXPIRED);
        assertThat(output.getOut()).contains("logType=security", "expired certificate, reason: EXPIRED");
    }

    @Test
    void validateCertificateUnknown(CapturedOutput output) {
        var anchorCert = Set.of(TestCerts.INVALID_SIGNATURE);

        assertThatThrownBy(() -> validator.validate(anchorCert, List.of(TestCerts.INVALID_SIGNATURE)))
                .asInstanceOf(InstanceOfAssertFactories.type(CertPathValidatorException.class))
                .extracting(CertPathValidatorException::getReason).isEqualTo(CertPathValidatorException.BasicReason.INVALID_SIGNATURE);
        assertThat(output.getOut()).contains("logType=security", "untrusted certificate, reason: INVALID_SIGNATURE");
    }
}
