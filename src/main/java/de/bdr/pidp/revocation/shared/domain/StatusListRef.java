/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.domain;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record StatusListRef(URI uri, int index) {

    public String generateIdentifier() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not get message digest instance", e);
        }

        md.update(uri.toString().getBytes(StandardCharsets.UTF_8));
        md.update(Integer.toString(index).getBytes(StandardCharsets.UTF_8));

        var hash = md.digest();
        return HexFormat.of().formatHex(hash);
    }
}
