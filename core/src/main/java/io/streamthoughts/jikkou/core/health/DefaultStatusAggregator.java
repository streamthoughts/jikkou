/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultStatusAggregator implements StatusAggregator {

    private static final String[] DEFAULT_ORDERED_STATUS;

    static {
        DEFAULT_ORDERED_STATUS = new String[] {
                HealthStatus.DOWN.name(),
                HealthStatus.UP.name(),
                HealthStatus.UNKNOWN.name()
        };
    }

    private final List<String> statusOrder;

    /**
     * Creates a new {@link DefaultStatusAggregator} instance.
     */
    public DefaultStatusAggregator() {
        this.statusOrder = Arrays.asList(DEFAULT_ORDERED_STATUS);
    }

    /**
     * Creates a new {@link DefaultStatusAggregator} instance using the specified status order.
     *
     * @param statusOrder   the {@link HealthStatus} to order to be used for aggregating {@link HealthStatus}.
     */
    private DefaultStatusAggregator(final List<HealthStatus> statusOrder) {
        Objects.requireNonNull(statusOrder, "statusOrder cannot be null");
        this.statusOrder = statusOrder.stream().map(HealthStatus::name).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthStatus aggregateStatus(final List<HealthStatus> allStatus) {
        return allStatus
                .stream()
                .min(new StatusComparator())
                .orElse(HealthStatus.UNKNOWN);
    }

    private class StatusComparator implements Comparator<HealthStatus> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(HealthStatus status1, HealthStatus status2) {
            int i1 = statusOrder.indexOf(status1.name());
            int i2 = statusOrder.indexOf(status2.name());
            return (i1 < i2) ? -1 : (i1 != i2) ? 1 : status1.name().compareTo(status2.name());
        }
    }
}