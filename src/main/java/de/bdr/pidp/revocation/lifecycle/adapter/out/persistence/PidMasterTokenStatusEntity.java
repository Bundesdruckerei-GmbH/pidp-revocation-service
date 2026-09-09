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
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "pid_master_token_status")
public class PidMasterTokenStatusEntity {

    public PidMasterTokenStatusEntity(String tokenID, String pseudonym, Instant expirationTime) {
        this.tokenID = tokenID;
        this.pseudonym = pseudonym;
        this.expirationTime = expirationTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "token_id", nullable = false)
    private String tokenID;

    @Column(name = "pseudonym", nullable = false)
    private String pseudonym;

    @Column(name = "expiration", nullable = false)
    private Instant expirationTime;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::lifecycle_status")
    private LifecycleStatus lifecycleStatus = LifecycleStatus.VALID;

    @Column(name = "wallet_instance_reference")
    private String walletInstanceRef;
}
