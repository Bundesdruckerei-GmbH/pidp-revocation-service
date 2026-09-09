/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "processed_wallet_status_list_token")
@NoArgsConstructor
public class ProcessedWalletStatusListTokenEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "verification_run_id", nullable = false)
    private String verificationRunID;

    @ManyToOne
    @JoinColumn(name = "status_list_reference_id", nullable = false)
    private StatusListRefEntity statusListRefEntity;

    @Column(name = "revoked_indexes", columnDefinition = "int[]")
    @Type(CustomIntArrayType.class)
    private Integer[] revokedIndexes;

    public ProcessedWalletStatusListTokenEntity(String verificationRunID, StatusListRefEntity statusListRefEntity, Integer[] revokedIndexes) {
        this.verificationRunID = verificationRunID;
        this.statusListRefEntity = statusListRefEntity;
        this.revokedIndexes = revokedIndexes;
    }
}
