/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import de.bdr.pidp.revocation.shared.jwt.JWTClaimsVerifier;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;

public class StatusListTokenClaimsVerifier extends JWTClaimsVerifier<StatusListTokenSecurityContext> {

    StatusListTokenClaimsVerifier(int maxClockSkew) {
        setMaxClockSkew(maxClockSkew);
    }

    @Override
    public void verify(JWTClaimsSet claimsSet, StatusListTokenSecurityContext context) throws BadJWTException {
        verifySubjectClaim(claimsSet, context);

        verifyTimeClaims(claimsSet);

        final var ttl = verifyTimeToLiveClaimIfExists(claimsSet);
        context.setTimeToLive(ttl);

        final var statusList = verifyStatusListClaim(claimsSet);
        context.setStatusList(statusList);
    }

    private void verifyTimeClaims(JWTClaimsSet claimsSet) throws BadJWTException {
        final var now = new Date();

        verifyIssuedAtClaim(claimsSet, now);

        verifyExpirationClaimIfPresent(claimsSet, now);
    }

    private void verifySubjectClaim(JWTClaimsSet claimsSet, StatusListTokenSecurityContext context) throws BadJWTException {
        verifySubjectClaimExists(claimsSet);

        final var sub = claimsSet.getSubject();

        if (!context.getTokenURI().equals(URI.create(sub))) {
            throw new BadJWTException("Subject does not match status list token URI");
        }
    }

    private @Nullable Integer verifyTimeToLiveClaimIfExists(JWTClaimsSet claimsSet) throws BadJWTException {
        final Integer ttl;
        try {
            ttl = claimsSet.getIntegerClaim("ttl");
        } catch (ParseException e) {
            throw new BadJWTException("Invalid time to live (ttl): could not be parsed", e);
        }

        if (ttl != null && ttl < 0) {
            throw new BadJWTException("Time to live (ttl) is negative");
        }

        return ttl;
    }
}
