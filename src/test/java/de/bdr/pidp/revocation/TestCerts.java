/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation;

import com.nimbusds.jose.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TestCerts {

    public static final X509Certificate CHAIN_LEAF_PUB = readCertificate("/certificates/chain/test_leaf.cer");
    public static final PrivateKey CHAIN_LEAF_PRIV = readPrivateKey("/certificates/chain/test_leaf.pem", "EC");

    public static final X509Certificate CHAIN_TRUST_ANCHOR = readCertificate("/certificates/chain/test_ca.cer");
    public static final Set<X509Certificate> CHAIN_TRUST_ANCHOR_SINGLETON_SET = Collections.singleton(CHAIN_TRUST_ANCHOR);

    public static final List<X509Certificate> CHAIN_EXCLUDING_TRUST_ANCHOR = List.of(
        readCertificate("/certificates/chain/test_leaf.cer"),
        readCertificate("/certificates/chain/test_ca_sub.cer")
    );

    public static final List<Base64> CHAIN_EXCLUDING_TRUST_ANCHOR_B64 = List.of(
        readCertificateB64("/certificates/chain/test_leaf.cer"),
        readCertificateB64("/certificates/chain/test_ca_sub.cer")
    );

    public static final X509Certificate SELF_SIGNED = readCertificate("/certificates/selfsigned/test_self_signed.cer");
    public static final Set<X509Certificate> SELF_SIGNED_SINGLETON_SET = Collections.singleton(SELF_SIGNED);

    public static final Base64 SELF_SIGNED_B64 = readCertificateB64("/certificates/selfsigned/test_self_signed.cer");

    public static final PrivateKey SELF_SIGNED_PRIV = readPrivateKey("/certificates/selfsigned/test_self_signed.pem", "EC");

    public static final X509Certificate UNKNOWN_LEAF = readCertificate("/certificates/invalid/test_unknown_leaf.cer");
    public static final Base64 UNKNOWN_LEAF_B64 = readCertificateB64("/certificates/invalid/test_unknown_leaf.cer");

    public static final X509Certificate INVALID_SIGNATURE = readCertificate("/certificates/invalid/test_invalid_signature.crt");

    public static final X509Certificate EXPIRED_TRUST_ANCHOR = readCertificate("/certificates/invalid/test_expired_ca.cer");
    public static final X509Certificate EXPIRED_LEAF = readCertificate("/certificates/invalid/test_expired_leaf.cer");

    private static X509Certificate readCertificate(String resourcePath) {
        try {
            var is = TestCerts.class.getResourceAsStream(resourcePath);
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private static Base64 readCertificateB64(String resourcePath) {
        try {
            return Base64.encode(readCertificate(resourcePath).getEncoded());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey readPrivateKey(String resourcePath, String algorithm) {
        try (var resource = TestCerts.class.getResourceAsStream(resourcePath)) {
            Objects.requireNonNull(resource, "JWT Private Key could not be found");

            byte[] tmp = resource.readAllBytes();
            return decodePrivateKey(new String(tmp, StandardCharsets.UTF_8), algorithm);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static PrivateKey decodePrivateKey(final String pemEncoded, final String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(keySpec);
    }

    private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return java.util.Base64.getDecoder().decode(normalizedPem);
    }

    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("[\r\n]", "");
        return pem.trim();
    }
}
