/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.jwt;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ClockSkewAware;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import com.nimbusds.jwt.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.util.Date;

public abstract class JWTClaimsVerifier<T extends SecurityContext> implements JWTClaimsSetVerifier<T>, ClockSkewAware {

    @Getter
    @Setter
    private int maxClockSkew;

    protected void verifyIssuedAtClaim(JWTClaimsSet claimsSet, Date nowRef) throws BadJWTException {
        final Date iat = claimsSet.getIssueTime();

        if (iat == null) {
            throw new BadJWTException("Missing JWT issue time (iat) claim");
        }

        if (!(DateUtils.isBefore(iat, nowRef, maxClockSkew))) {
            throw new BadJWTException("JWT issue time ahead of current time");
        }
    }

    protected void verifyExpirationClaimIfPresent(JWTClaimsSet claimsSet, Date nowRef) throws BadJWTException {
        final Date exp = claimsSet.getExpirationTime();

        if (exp != null && !DateUtils.isAfter(exp, nowRef, maxClockSkew)) {
            throw new BadJWTException("Expired JWT");
        }
    }

    protected void verifySubjectClaimExists(JWTClaimsSet claimsSet) throws BadJWTException {
        final var sub = claimsSet.getSubject();

        if (sub == null || sub.isEmpty()) {
            throw new BadJWTException("Missing JWT subject (sub) claim");
        }
    }

    protected StatusList verifyStatusListClaim(JWTClaimsSet claimsSet) throws BadJWTException {
        final StatusList statusList;
        try {
            statusList = StatusList.parse(claimsSet);
        } catch (ParseException e) {
            throw new BadJWTException("Invalid status list: could not be parsed", e);
        }

        if (statusList == null) {
            throw new BadJWTException("Missing JWT status list");
        }

        return statusList;
    }
}
