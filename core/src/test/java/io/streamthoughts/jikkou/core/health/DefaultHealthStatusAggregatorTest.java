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

class DefaultHealthStatusAggregatorTest {

    private final DefaultStatusAggregator aggregator = new DefaultStatusAggregator();

    @Test
    void should_aggregate_status_given_up_and_unknown() {
        HealthStatus status = aggregator.aggregateStatus(List.of(HealthStatus.UP, HealthStatus.UNKNOWN));
        Assertions.assertEquals(HealthStatus.UP, status);
    }

    @Test
    void should_aggregate_status_given_up_and_down() {
        HealthStatus status = aggregator.aggregateStatus(List.of(HealthStatus.UP, HealthStatus.DOWN));
        Assertions.assertEquals(HealthStatus.DOWN, status);
    }

    @Test
    void should_aggregate_status_given_down_and_unknown() {
        HealthStatus status = aggregator.aggregateStatus(List.of(HealthStatus.DOWN, HealthStatus.UNKNOWN));
        Assertions.assertEquals(HealthStatus.DOWN, status);
    }
}