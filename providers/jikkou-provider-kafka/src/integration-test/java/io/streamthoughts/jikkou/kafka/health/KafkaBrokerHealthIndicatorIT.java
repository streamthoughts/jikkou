/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.health;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthStatus;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
class KafkaBrokerHealthIndicatorIT extends AbstractKafkaIntegrationTest {

    private AdminClientContextFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new AdminClientContextFactory(
                Configuration.empty(),
                () -> AdminClient.create(clientConfig())
        );
    }

    @Test
    void shouldGetKafkaHealth() {
        var indicator = new KafkaBrokerHealthIndicator(factory);
        Health health = indicator.getHealth(Duration.ofSeconds(30));
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.UP, health.getStatus());
        Assertions.assertTrue(((String) health.getDetails().get("resource")).startsWith("urn:kafka:cluster:id:"));
        Assertions.assertEquals(1, ((List) health.getDetails().get("brokers")).size());
        Assertions.assertEquals(
                ("{id=1, host=" + getBrokerHost() + ", port=" + getBrokerPort() + "}"),
                ((List) health.getDetails().get("brokers"))
                        .get(0)
                        .toString()
        );
    }
}