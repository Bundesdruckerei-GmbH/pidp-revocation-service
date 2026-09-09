/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "wallet_instance_status_reference")
public class WalletInstanceStatusRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "wallet_instance_reference", nullable = false)
    private String walletInstanceReference;

    @Column(name = "status_list_index", nullable = false)
    private int statusListIndex;

    @ManyToOne
    @JoinColumn(name = "status_list_reference_id", nullable = false)
    private StatusListRefEntity statusListRefEntity;

    public WalletInstanceStatusRefEntity() {
    }

    public WalletInstanceStatusRefEntity(String walletInstanceReference, int statusListIndex, StatusListRefEntity statusListRefEntity) {
        this.walletInstanceReference = walletInstanceReference;
        this.statusListIndex = statusListIndex;
        this.statusListRefEntity = statusListRefEntity;
    }
}
