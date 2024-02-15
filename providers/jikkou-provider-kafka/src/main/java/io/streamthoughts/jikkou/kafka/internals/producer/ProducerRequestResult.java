/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.producer;

import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ProducerRequestResult<K, V>(@NotNull KafkaRecord<K, V> record,
                                          @Nullable Integer partition,
                                          @Nullable Long offset,
                                          @Nullable Long timestamp,
                                          @Nullable Throwable error) {

    public ProducerRequestResult(@NotNull KafkaRecord<K, V> record,
                                 @NotNull Integer partition,
                                 @NotNull Long offset,
                                 @NotNull Long timestamp) {
        this(record, partition, offset, timestamp, null);
    }

    public ProducerRequestResult(@NotNull KafkaRecord<K, V> record,
                                 @NotNull Throwable error) {
        this(record, null, null, null, error);
    }

}
