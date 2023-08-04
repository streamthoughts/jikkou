/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.health;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HealthAggregatorTest {

    private final HealthAggregator aggregator = new HealthAggregator();

    @Test
    void should_successfully_aggregate_healths_with_name() {
        var aggregate = aggregator.aggregate("test", List.of(
                Health.builder().up().withName("OK").build(),
                Health.builder().down().withName("KO").build()
        ));

        Status status = aggregate.getStatus();
        Assertions.assertEquals("test", aggregate.getName());
        Assertions.assertEquals(Status.DOWN, status);
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