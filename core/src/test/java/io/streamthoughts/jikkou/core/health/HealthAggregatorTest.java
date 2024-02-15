/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HealthAggregatorTest {

    private final HealthAggregator aggregator = new HealthAggregator();

    @Test
    void should_successfully_aggregate_healths_with_name() {
        var aggregate = aggregator.aggregate("test", List.of(
                Health.builder().up().name("OK").build(),
                Health.builder().down().name("KO").build()
        ));

        HealthStatus status = aggregate.getStatus();
        Assertions.assertEquals("test", aggregate.getName());
        Assertions.assertEquals(HealthStatus.DOWN, status);
    }

    @Test
    void should_fail_aggregate_healths_without_name() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            aggregator.aggregate("test", List.of(
                    Health.builder().up().build()
            ));
        });
        Assertions.assertEquals("Cannot aggregate metric with empty name", exception.getMessage());
    }

}