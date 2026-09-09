/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.shared.jwt;

import com.nimbusds.jose.proc.SecurityContext;

public interface StatusListSecurityContext extends SecurityContext {
    StatusList getStatusList();
}
