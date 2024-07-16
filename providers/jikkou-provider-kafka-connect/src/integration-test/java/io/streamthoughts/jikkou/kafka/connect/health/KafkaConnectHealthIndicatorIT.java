/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.health;

import io.streamthoughts.jikkou.core.health.HealthStatus;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.kafka.connect.BaseExtensionProviderIT;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaConnectHealthIndicatorIT extends BaseExtensionProviderIT {

    @Test
    void shouldGetHealthIndicator() {
        ApiHealthResult result = api.getApiHealth("kafkaconnect", Duration.ZERO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HealthStatus.UP, result.status());
    }
}