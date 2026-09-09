/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.net.URI;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "pid_credential_lifecycle")
public class PidCredentialLifecycleEntity {

    public PidCredentialLifecycleEntity(URI listID, int listIndex, Instant expirationTime, PidMasterTokenStatusEntity masterTokenStatus) {
        this.listID = listID;
        this.listIndex = listIndex;
        this.expirationTime = expirationTime;
        this.masterTokenStatus = masterTokenStatus;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "list_id", nullable = false)
    private URI listID;

    @Column(name = "list_index", nullable = false)
    private int listIndex;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::lifecycle_status")
    private LifecycleStatus status = LifecycleStatus.VALID;

    @Column(name = "expiration_time", nullable = false)
    private Instant expirationTime;

    @ManyToOne
    @JoinColumn(name = "pid_master_token_status_id", nullable = false)
    private PidMasterTokenStatusEntity masterTokenStatus;
}
