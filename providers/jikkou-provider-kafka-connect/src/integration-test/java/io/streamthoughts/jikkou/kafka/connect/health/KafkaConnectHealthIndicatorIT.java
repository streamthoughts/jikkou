/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.health;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthStatus;
import io.streamthoughts.jikkou.kafka.connect.AbstractKafkaConnectorIT;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionConfig;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaConnectHealthIndicatorIT extends AbstractKafkaConnectorIT {

    @Test
    void shouldGetHealthIndicator() {
        KafkaConnectClientConfig configuration = new KafkaConnectClientConfig(Configuration.builder()
                .with("name", KAFKA_CONNECTOR_NAME)
                .with(KafkaConnectClientConfig.KAFKA_CONNECT_URL.key(), getConnectUrl())
                .build());
        KafkaConnectHealthIndicator indicator = new KafkaConnectHealthIndicator();
        indicator.configure(new KafkaConnectExtensionConfig(List.of(configuration)));

        Health health = indicator.getHealth(Duration.ZERO);
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.UP, health.getStatus());
    }
}