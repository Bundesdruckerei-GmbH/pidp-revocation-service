/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.domain;

import java.net.URI;
import java.time.Instant;

public record CredentialInfo(Instant expiration, URI uri, int index) {
}
