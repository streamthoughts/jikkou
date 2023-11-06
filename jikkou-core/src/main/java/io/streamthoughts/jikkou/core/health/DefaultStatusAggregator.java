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