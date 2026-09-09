/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.app.domain;

public class StatusListNotFoundException extends RuntimeException {
    public StatusListNotFoundException(String message) {
        super(message);
    }
}
