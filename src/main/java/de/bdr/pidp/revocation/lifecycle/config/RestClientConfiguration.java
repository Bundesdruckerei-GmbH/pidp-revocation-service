/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.config;

import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.ApiClient;
import de.bdr.pidp.revocation.lifecycle.adapter.out.rest.api.DefaultApi;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

@Getter
@Setter
@Validated
@NullMarked
@Configuration
@ConfigurationProperties(prefix = "statuslistservice.restclient")
public class RestClientConfiguration {

    private String apiBasePath;

    private String apiKey;

    @Bean
    public ApiClient apiClient(@Valid RestClient.Builder builder) {
        var apiClient = new ApiClient(builder.build());
        apiClient.setBasePath(apiBasePath);
        apiClient.setApiKey(apiKey);
        return apiClient;
    }

    @Bean
    public DefaultApi statusListServiceClient(@Valid ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
