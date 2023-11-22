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
class KafkaConnectHealthIndicatorTest extends AbstractKafkaConnectorIT {

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