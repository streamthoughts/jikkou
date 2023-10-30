/*
 * Copyright 2022 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.extension.aiven.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.io.Jackson;
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
@Description("Get the health of an Aiven service")
public final class AivenServiceHealthIndicator implements HealthIndicator, Configurable {

    private static final String HEALTH_NAME = "avnservice";

    private AivenApiClientConfig config;

    /**
     * Creates a new {@link AivenServiceHealthIndicator} instance.
     * Empty constructor required for CLI.
     */
    public AivenServiceHealthIndicator() {
    }

    /**
     * Creates a new {@link AivenServiceHealthIndicator} instance.
     *
     * @param config the configuration.
     */
    public AivenServiceHealthIndicator(@NotNull AivenApiClientConfig config) {
        this.config = config;
    }

    /**
     * Creates a new {@link AivenServiceHealthIndicator} instance.
     *
     * @param configuration the context configuration.
     */
    public AivenServiceHealthIndicator(@NotNull Configuration configuration) {
        configure(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new AivenApiClientConfig(config));
    }

    public void configure(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health getHealth(final Duration timeout) {
        if (config == null) {
            throw new IllegalStateException("must be configured!");
        }
        final AivenApiClient api = AivenApiClientFactory.create(config);
        try {
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
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }

    private String getUrn() {
        return String.format("urn:aiven:project:%s:service:%s", config.getProject(), config.getService());
    }
}
