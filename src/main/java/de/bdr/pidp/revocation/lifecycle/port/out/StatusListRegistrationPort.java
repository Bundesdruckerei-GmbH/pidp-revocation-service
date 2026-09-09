/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.port.out;

import de.bdr.pidp.revocation.shared.domain.StatusListRef;

public interface StatusListRegistrationPort {
    Registration register(StatusListRef walletInstanceStatusListRef);

    record Registration(String walletInstanceRef) {
    }
}
