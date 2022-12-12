/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultStatusAggregator implements StatusAggregator {

    private static final String[] DEFAULT_ORDERED_STATUS;

    static {
        DEFAULT_ORDERED_STATUS = new String[] {
                Status.DOWN.code(),
                Status.UP.code(),
                Status.UNKNOWN.code()
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
     * @param statusOrder   the {@link Status} to order to be used for aggregating {@link Status}.
     */
    private DefaultStatusAggregator(final List<Status> statusOrder) {
        Objects.requireNonNull(statusOrder, "statusOrder cannot be null");
        this.statusOrder = statusOrder.stream().map(Status::code).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status aggregateStatus(final List<Status> allStatus) {
        return allStatus
                .stream()
                .min(new StatusComparator())
                .orElse(Status.UNKNOWN);
    }

    private class StatusComparator implements Comparator<Status> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Status status1, Status status2) {
            int i1 = statusOrder.indexOf(status1.code());
            int i2 = statusOrder.indexOf(status2.code());
            return (i1 < i2) ? -1 : (i1 != i2) ? 1 : status1.code().compareTo(status2.code());
        }
    }
}