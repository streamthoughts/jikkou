/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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