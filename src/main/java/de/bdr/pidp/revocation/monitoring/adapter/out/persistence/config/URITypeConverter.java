/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.net.URI;

@Converter(autoApply = true)
public class URITypeConverter implements AttributeConverter<URI, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable URI attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public @Nullable URI convertToEntityAttribute(@Nullable String dbData) {
        return StringUtils.hasText(dbData) ? URI.create(dbData) : null;
    }
}
