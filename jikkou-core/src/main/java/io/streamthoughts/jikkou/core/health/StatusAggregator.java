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
package io.streamthoughts.jikkou.core.health;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link StatusAggregator} is used to aggregate multiple {@link HealthStatus} instance.
 *
 * @see HealthAggregator
 */
public interface StatusAggregator {

    /**
     * Aggregates the specified list of status.
     *
     * @param allStatus the list of {@link HealthStatus} to be aggregate.
     *
     * @return          the aggregated {@link HealthStatus} instance.
     */
    HealthStatus aggregateStatus(final List<HealthStatus> allStatus);


    /**
     * Static helper that can be used for retrieving only status from a list of {@link Health} instances.
     *
     * @param healths   the list of {@link Health}.
     * @return          the corresponding list of {@link HealthStatus}.
     */
    static List<HealthStatus> getAllStatus(final Collection<Health> healths) {
        return healths.stream().map(Health::getStatus).collect(Collectors.toList());
    }
}