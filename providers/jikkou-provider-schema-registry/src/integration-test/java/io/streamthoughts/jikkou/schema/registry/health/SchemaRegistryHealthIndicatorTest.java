/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.health;

import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaRegistryHealthIndicatorTest extends AbstractIntegrationTest {

    private SchemaRegistryHealthIndicator indicator;

    @BeforeEach
    public void beforeEach() {
        indicator = new SchemaRegistryHealthIndicator(getSchemaRegistryClientConfiguration());
    }

    @Test
    public void shouldGetHealthForServerUp() {
        // When
        Health health = indicator.getHealth(Duration.ZERO);

        // Then
        Health expected = Health.builder()
                .up()
                .name("schemaregistry")
                .details("schema.registry.url", getSchemaRegistryClientConfiguration().getSchemaRegistryUrl())
                .details("http.response.status", 200)
                .build();
        Assertions.assertEquals(expected, health);
    }
}