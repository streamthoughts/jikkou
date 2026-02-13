/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.health;

import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.schema.registry.BaseExtensionProviderIT;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryHealthIndicatorTest extends BaseExtensionProviderIT {

    @Test
    public void shouldGetHealthForServerUp() {
        // When
        ApiHealthResult result = api.getApiHealth(SchemaRegistryHealthIndicator.HEALTH_INDICATOR_NAME, Duration.ZERO);
        // Then
        Health expected = Health.builder()
            .up()
            .name("schemaregistry")
            .details("schema.registry.url", List.of(schemaRegistryUrl()))
            .details("http.response.status", 200)
            .build();
        Assertions.assertEquals(ApiHealthResult.from(expected), result);
    }
}