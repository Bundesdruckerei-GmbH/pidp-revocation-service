/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import de.bdr.pidp.revocation.shared.domain.RevocationServerException;
import de.bdr.pidp.revocation.shared.jwt.StatusList;
import de.bdr.pidp.revocation.shared.jwt.X5CJWSKeySelector;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Component
public class StatusListTokenVerifier {
    private static final JOSEObjectType STATUS_LIST_TYPE = new JOSEObjectType("statuslist+jwt");

    private final JWTProcessor<StatusListTokenSecurityContext> jwtProcessor;
    private final Set<X509Certificate> trustAnchors;

    @Autowired
    public StatusListTokenVerifier(@Value("${revocation.time-tolerance}") Duration timeTolerance) {
        this(Collections.emptySet(), timeTolerance);
    }

    public StatusListTokenVerifier(Set<X509Certificate> trustAnchors, Duration timeTolerance) {
        var algsSupported = JWSAlgorithm.Family.EC;
        var allowSelfSigned = false;
        jwtProcessor = buildStatusListJWTProcessor(algsSupported, allowSelfSigned, (int) timeTolerance.toSeconds());
        this.trustAnchors = trustAnchors;
    }

    public StatusListValues verify(SignedJWT statusListToken, URI tokenURI) {
        var ctx = new StatusListTokenSecurityContext(tokenURI, trustAnchors);
        try {
            jwtProcessor.process(statusListToken, ctx);
        } catch (BadJOSEException | KeySourceException e) {
            throw new RevocationServerException("Status List Token invalid: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new RevocationServerException("Status List Token invalid", e);
        }

        return new StatusListValues(
            ctx.getStatusList(),
            ctx.getTimeToLive()
        );
    }

    private static JWTProcessor<StatusListTokenSecurityContext> buildStatusListJWTProcessor(Set<JWSAlgorithm> jwsAlgorithmsSupported, boolean allowSelfSigned, int timeToleranceSeconds) {
        var processor = new DefaultJWTProcessor<StatusListTokenSecurityContext>();

        var typeVerifier = new DefaultJOSEObjectTypeVerifier<StatusListTokenSecurityContext>(STATUS_LIST_TYPE);
        processor.setJWSTypeVerifier(typeVerifier);

        var keySelector = new X5CJWSKeySelector<StatusListTokenSecurityContext>(jwsAlgorithmsSupported, allowSelfSigned);
        processor.setJWSKeySelector(keySelector);

        var claimsVerifier = new StatusListTokenClaimsVerifier(timeToleranceSeconds);
        processor.setJWTClaimsSetVerifier(claimsVerifier);

        return processor;
    }

    public record StatusListValues(StatusList statusList, @Nullable Integer timeToLive) {
    }
}
