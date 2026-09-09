/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.domain;

import java.time.Instant;

public record TokenInfo(String tokenID, String pseudonym, Instant expiration) {
}
