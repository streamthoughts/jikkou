/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import java.util.Collection;
import java.util.Optional;

/**
 * The {@link HealthAggregator} aggregates multiple {@link Health} instances into a single one.
 */
public final class HealthAggregator {

    private final StatusAggregator statusAggregator;

    public HealthAggregator() {
        this(new DefaultStatusAggregator());
    }

    public HealthAggregator(final StatusAggregator statusAggregator) {
        this.statusAggregator = statusAggregator;
    }

    /**
     * Aggregates the specified {@link Health} instances to a single one.
     *
     * @param healths the list of {@link Health} to aggregate.
     * @return the aggregated {@link Health}.
     */
    public Health aggregate(final Collection<Health> healths) {
        return aggregate(null, healths);
    }

    /**
     * Aggregates the specified {@link Health} instances to a single one.
     *
     * @param name    the aggregate health name.
     * @param healths the list of {@link Health} to aggregate.
     * @return the aggregated {@link Health}.
     */
    public Health aggregate(final String name, final Collection<Health> healths) {
        final Health.Builder builder = new Health.Builder().up();
        Optional.ofNullable(name).ifPresent(builder::name);
        if (!healths.isEmpty()) {
            healths.forEach(h -> {
                if (h.getName() == null || h.getName().isBlank()) {
                    throw new IllegalArgumentException("Cannot aggregate metric with empty name");
                }
                builder.details(
                                h.getName(),
                                // avoid redundancy by removing the health name
                                new Health.Builder()
                                        .status(h.getStatus())
                                        .details(h.getDetails())
                                        .build()
                        );
                    }
            );
            final HealthStatus status = statusAggregator.aggregateStatus(StatusAggregator.getAllStatus(healths));
            builder.status(status);
        }
        return builder.build();
    }

}