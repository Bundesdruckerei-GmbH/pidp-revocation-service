/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.jwt;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;

/// Class to handle Status List Token in JWT Format according to draft-ietf-oauth-status-list
/// ```
/// {
///   "status_list": {
///     "bits": 1,
///     "lst": "eNrbuRgAAhcBXQ"
///   }
/// }
/// ```
public record StatusList(int bits, String lst) {

    private static final String STATUS_LIST_CLAIM = "status_list";
    private static final String BITS_CLAIM = "bits";
    private static final String LIST_CLAIM = "lst";

    public static @Nullable StatusList parse(final JWTClaimsSet jwtClaimsSet) throws ParseException {
        var status = jwtClaimsSet.getJSONObjectClaim(STATUS_LIST_CLAIM);
        if (status == null) {
            return null;
        }
        var bits = JSONObjectUtils.getInt(status, BITS_CLAIM);
        var lst = JSONObjectUtils.getString(status, LIST_CLAIM);
        if (lst == null) {
            throw new ParseException("status_list expected to contain list (lst) claim", 0);
        }

        return new StatusList(bits, lst);
    }
}
