/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HealthTest {

    @Test
    void should_success_create_health_with_empty_name() {
        Health health = Health.builder().up().build();
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.UP, health.getStatus());
        Assertions.assertNull(health.getName());
    }

    @Test
    void should_success_create_health_up() {
        Health health = Health.builder()
                .up()
                .name("test")
                .build();
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.UP, health.getStatus());
        Assertions.assertEquals("test", health.getName());
    }

    @Test
    void should_success_create_health_down_given_exception() {
        Health health = Health.builder()
                .down()
                .exception(new RuntimeException("Boom!"))
                .build();
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.DOWN, health.getStatus());
        Assertions.assertNotNull(health.getDetails());
        Assertions.assertEquals("java.lang.RuntimeException: Boom!", health.getDetails().get("error"));
    }

    @Test
    void should_success_create_health_given_status_and_details() {
        Health health = Health.builder()
                .status(HealthStatus.UNKNOWN)
                .details("key", "value")
                .build();
        Assertions.assertNotNull(health);
        Assertions.assertEquals(HealthStatus.UNKNOWN, health.getStatus());
        Assertions.assertNotNull(health.getDetails());
        Assertions.assertEquals("value", health.getDetails().get("key"));
    }
}