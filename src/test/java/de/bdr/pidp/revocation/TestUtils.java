/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;

import java.net.URI;
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class TestUtils {

    public static final JOSEObjectType STATUS_LIST_TYPE = new JOSEObjectType("statuslist+jwt");

    public static JWTClaimsSet.Builder getStatusListClaimsBuilderWithDefaults(URI statusListURI) {
        var now = Instant.now();
        return new JWTClaimsSet.Builder()
            .subject(statusListURI.toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(30L)))
            .claim("ttl", 300)
            .claim("status_list", new JSONObject()
                .appendField("bits", 1)
                .appendField("lst", Base64URL.encode(new byte[8]).toString())
            );
    }

    public static SignedJWT buildStatusListToken(JWTClaimsSet claims, JOSEObjectType type) {
        return buildX5CJWT(claims, type, (ECPrivateKey) TestCerts.CHAIN_LEAF_PRIV, TestCerts.CHAIN_EXCLUDING_TRUST_ANCHOR_B64);
    }

    public static SignedJWT buildValidStatusListToken(URI statusListURI) {
        var claims = getStatusListClaimsBuilderWithDefaults(statusListURI).build();
        return buildStatusListToken(claims, STATUS_LIST_TYPE);
    }

    public static SignedJWT buildX5CJWT(JWTClaimsSet claims, JOSEObjectType type, ECPrivateKey privateKey, List<Base64> certChain) {
        var algorithm = JWSAlgorithm.ES256;
        var header = new JWSHeader.Builder(algorithm)
            .x509CertChain(certChain)
            .type(type)
            .build();
        var signedJWT = new SignedJWT(header, claims);
        try {
            var signer = new ECDSASigner(privateKey);
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return signedJWT;
    }
}
