/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class StatusListRefTest {

    @Test
    void shouldGenerateIdentifier() {
        // Given
        URI uri = URI.create("https://bdr.de/status-list");
        int index = 1;
        var statusListRef = new StatusListRef(uri, index);

        // When
        var generatedIdentifier = statusListRef.generateIdentifier();

        // Then
        var expectedIdentifier = "9a16346e6ec4412f82eeac8758474e2b3235fabfad6c664bb346e903b3371668";
        assertThat(generatedIdentifier).isEqualTo(expectedIdentifier);
    }
}
