/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.app.domain;

public class TokenIDAlreadyExistsException extends RuntimeException {
    public TokenIDAlreadyExistsException(String message) {
        super(message);
    }
}
