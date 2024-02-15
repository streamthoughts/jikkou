/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.health;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
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
@Title("SchemaRegistryHealthIndicator allows checking whether the Schema Registry is healthy.")
@Description("Get the health of Schema Registry")
public final class SchemaRegistryHealthIndicator implements HealthIndicator {

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
    public void init(@NotNull final ExtensionContext context) throws ConfigException {
        this.config = new SchemaRegistryClientConfig(context.appConfiguration());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Health getHealth(Duration timeout) {
        SchemaRegistryApi api = SchemaRegistryApiFactory.create(config);
        try {
            Health.Builder builder = Health.builder()
                    .name(HEALTH_INDICATOR_NAME);
            Response response = null;
            try {
                response = api.get();
                if (response.getStatus() == 200) {
                    builder = builder.up();
                } else {
                    builder = builder.down();
                }
            } catch (Exception e) {
                builder = builder.down().exception(e);
            }
            if (response != null) {
                builder = builder.details("http.response.status", response.getStatus());
            }
            return builder
                    .details("schema.registry.url", config.getSchemaRegistryUrl())
                    .build();
        } finally {
            api.close();
        }
    }
}
