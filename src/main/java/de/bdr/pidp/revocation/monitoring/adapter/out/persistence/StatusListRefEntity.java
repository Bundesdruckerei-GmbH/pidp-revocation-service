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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
@Entity
@Table(name = "status_list_reference")
public class StatusListRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uri", nullable = false)
    private URI uri;

    public StatusListRefEntity() {
    }

    public StatusListRefEntity(URI uri) {
        this.uri = uri;
    }
}
