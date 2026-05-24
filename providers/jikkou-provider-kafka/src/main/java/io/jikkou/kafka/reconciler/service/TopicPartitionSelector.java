/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import java.util.OptionalInt;

public record TopicPartitionSelector(String topic, OptionalInt partition) {

    public static TopicPartitionSelector parse(String raw) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public boolean isAllPartitions() {
        return partition.isEmpty();
    }
}
