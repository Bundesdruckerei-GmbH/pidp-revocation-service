/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.domain;

import com.nimbusds.jose.util.Base64URL;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

@Slf4j
public record StatusList(int bits, String lst) {

    public StatusList(de.bdr.pidp.revocation.shared.jwt.StatusList jwt) {
        this(jwt.bits(),  jwt.lst());
    }

    public List<Integer> getRevokedIndexes() {
        var compressed = Base64URL.from(lst).decode();
        try (InflaterInputStream stream = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
            byte[] statusList = stream.readAllBytes();

            // number of status flags in list
            var listSize = statusList.length * (8 / bits);

            // bitmask with number of bits per status set to 1
            var mask = ((1 << bits) - 1);

            var revokedIndexes = new ArrayList<Integer>();
            // we interpret all index values != 0 as revoked
            for (int i = 0; i < listSize; i++) {
                if (getIndexValue(i, mask, statusList) > 0) {
                    revokedIndexes.add(i);
                }
            }
            return revokedIndexes;
        } catch (IOException ioe) {
            log.error("Could not extract statuslist from compressed input.", ioe);
            throw new IllegalStateException("Could not extract statuslist from compressed input.", ioe);
        }
    }

    private int getIndexValue(int index, int mask, byte[] list) {
        var byteIndex = index * bits / 8;
        var shift = index * bits % 8;
        return ((list[byteIndex] & 0xff) & (mask << shift)) >> shift;
    }
}
