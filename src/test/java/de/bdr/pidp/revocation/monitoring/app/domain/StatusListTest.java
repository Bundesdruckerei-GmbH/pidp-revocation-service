/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.domain;

import com.nimbusds.jose.util.Base64URL;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusListTest {

    @Test
    void testGetRevokedIndexesWith1Bit() throws IOException {
        byte[] data = new byte[]{(byte) 0b00000101, (byte) 0b01100000}; // Bits 0, 2, 13 an 14 are set
        String lst = encodeAndCompress(data);
        StatusList token = new StatusList(1, lst);

        List<Integer> revoked = token.getRevokedIndexes();

        assertEquals(List.of(0, 2, 13, 14), revoked);
    }

    @Test
    void testGetRevokedIndexesWith2Bits() throws IOException {
        // 2 bits per status.
        // byte 0: [bit 1|bit 0] [bit 3|bit 2] [bit 5|bit 4] [bit 7|bit 6]
        // We want index 0 = 0 (00), index 1 = 1 (01), index 2 = 3 (11), index 3 = 2 (10)
        // byte 0 = 0b10110100 (Index 3: 10, Index 2: 11, Index 1: 01, Index 0: 00)
        // Bits:
        // Index 0: bits 0,1 -> 00 (0)
        // Index 1: bits 2,3 -> 01 (1)
        // Index 2: bits 4,5 -> 11 (3)
        // Index 3: bits 6,7 -> 10 (2)

        byte[] data = new byte[]{(byte) 0b10110100};
        String lst = encodeAndCompress(data);
        StatusList token = new StatusList(2, lst);

        List<Integer> revoked = token.getRevokedIndexes();
        assertEquals(List.of(1, 2, 3), revoked);
    }

    private String encodeAndCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(data);
        }
        return Base64URL.encode(baos.toByteArray()).toString();
    }
}
