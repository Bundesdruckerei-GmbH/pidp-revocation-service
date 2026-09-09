/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.http;

import com.nimbusds.jose.proc.SecurityContext;
import de.bdr.pidp.revocation.shared.jwt.StatusList;
import de.bdr.pidp.revocation.shared.jwt.StatusListSecurityContext;
import de.bdr.pidp.revocation.shared.jwt.TimeToLiveSecurityContext;
import de.bdr.pidp.revocation.shared.jwt.X509SecurityContext;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Set;

public class StatusListTokenSecurityContext implements StatusListSecurityContext, TimeToLiveSecurityContext, X509SecurityContext, SecurityContext {

    @Getter
    private final URI tokenURI;

    @Getter
    private final Set<X509Certificate> trustAnchorCertificates;

    @Setter
    @Nullable
    private StatusList statusList;

    @Setter
    @Nullable
    private Integer timeToLive;

    public StatusListTokenSecurityContext(URI tokenURI, Set<X509Certificate> trustAnchorCertificates) {
        this.tokenURI = tokenURI;
        this.trustAnchorCertificates = trustAnchorCertificates;
    }

    @Override
    public StatusList getStatusList() {
        if (statusList == null) {
            throw new IllegalStateException("status list not yet set");
        }

        return statusList;
    }

    @Override
    public @Nullable Integer getTimeToLive() {
        return timeToLive;
    }
}
