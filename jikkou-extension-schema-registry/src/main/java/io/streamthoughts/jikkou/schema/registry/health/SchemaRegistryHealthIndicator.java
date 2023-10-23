/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.schema.registry.health;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;

/**
 * Health indicator for Schema Registry component.
 */
@Named("schemaregistry")
@Description("Get the health of Schema Registry")
public final class SchemaRegistryHealthIndicator implements HealthIndicator, Configurable {

    private static final String HEALTH_INDICATOR_NAME = "schemaregistry";
    private SchemaRegistryClientConfig config;

    /**
     * Creates a new {@link SchemaRegistryHealthIndicator} instance.
     */
    public SchemaRegistryHealthIndicator() {
    }

    /**
     * Creates a new {@link SchemaRegistryHealthIndicator} instance.
     *
     * @param config    the configuration.
     */
    public SchemaRegistryHealthIndicator(SchemaRegistryClientConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = new SchemaRegistryClientConfig(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Health getHealth(Duration timeout) {
        SchemaRegistryApi api = SchemaRegistryApiFactory.create(config);
        try {
            Health.Builder builder = Health.builder()
                    .withName(HEALTH_INDICATOR_NAME);
            Response response = null;
            try {
                response = api.get();
                if (response.getStatus() == 200) {
                    builder = builder.up();
                } else {
                    builder = builder.down();
                }
            } catch (RestClientException e) {
                builder = builder.down().withException(e);
            }
            if (response != null) {
                builder = builder.withDetails("http.response.status", response.getStatus());
            }
            return builder
                    .withDetails("schema.registry.url", config.getSchemaRegistryUrl())
                    .build();
        } finally {
            api.close();
        }
    }
}
