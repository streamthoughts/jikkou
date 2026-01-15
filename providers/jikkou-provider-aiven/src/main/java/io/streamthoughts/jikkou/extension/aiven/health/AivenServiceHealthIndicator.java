/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ServiceInformationResponse;
import io.streamthoughts.jikkou.http.client.RestClientException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Aiven Service Health indicator.
 */
@Named("avnservice")
@Title("AivenServiceHealthIndicator allows checking whether the Aiven service is healthy.")
@Description("Get the health of an Aiven service")
public final class AivenServiceHealthIndicator implements HealthIndicator {

    private static final String HEALTH_NAME = "avnservice";

    private AivenApiClientConfig apiClientConfig;

    /**
     * Creates a new {@link AivenServiceHealthIndicator} instance.
     * Empty constructor required for CLI.
     */
    public AivenServiceHealthIndicator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.apiClientConfig = context.<AivenExtensionProvider>provider().apiClientConfig();
    }

    public void init(@NotNull AivenApiClientConfig apiClientConfig) throws ConfigException {
        this.apiClientConfig = apiClientConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health getHealth(final Duration timeout) {
        if (apiClientConfig == null || apiClientConfig.project() == null) {
            return Health
                    .builder()
                    .name(HEALTH_NAME)
                    .unknown()
                    .build();
        }
        try (AivenApiClient api = AivenApiClientFactory.create(apiClientConfig)) {
            ServiceInformationResponse response = api.getServiceInformation();
            if (!response.errors().isEmpty()) {
                return new Health.Builder()
                        .unknown()
                        .name(HEALTH_NAME)
                        .details(Map.of(
                                "message", response.message(),
                                "errors", response.errors()
                        ))
                        .build();
            }

            Map<String, Object> service = response.service();
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("cloud_name", service.get("cloud_name"));
            details.put("cloud_description", service.get("cloud_description"));
            details.put("metadata", service.get("metadata"));
            details.put("service_type", service.get("service_type"));
            details.put("service_uri", service.get("service_uri"));
            details.put("state", service.get("state"));
            details.put("plan", service.get("plan"));

            return new Health.Builder()
                    .up()
                    .name(HEALTH_NAME)
                    .details("resource", getUrn())
                    .details("service", details)
                    .build();
        } catch (RestClientException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponseEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponseEntity();
            }
            return new Health.Builder()
                    .down()
                    .name(HEALTH_NAME)
                    .details("resource", getUrn())
                    .details(Map.of("response", response))
                    .build();
        } catch (Exception e) {
            return new Health.Builder()
                    .down()
                    .name(HEALTH_NAME)
                    .details("resource", getUrn())
                    .details(Map.of("message", "An unexpected error has occurred while retrieving the information."))
                    .build();
        }
    }

    private String getUrn() {
        return String.format("urn:aiven:project:%s:service:%s", apiClientConfig.project(), apiClientConfig.service());
    }
}
