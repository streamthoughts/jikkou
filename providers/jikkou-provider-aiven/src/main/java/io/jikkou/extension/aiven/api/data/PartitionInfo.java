/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jikkou.core.annotation.Reflectable;

/**
 * Information about a single topic partition.
 *
 * <p>Offsets and size are 64-bit: Kafka offsets are {@code long}, and the partition size is reported in bytes,
 * so it exceeds the 32-bit range as soon as a partition grows past 2GiB.
 *
 * @param earliestOffset the earliest offset available in the partition.
 * @param latestOffset   the latest offset available in the partition.
 * @param partition      the partition index.
 * @param size           the partition size in bytes.
 */
@Reflectable
public record PartitionInfo(
    @JsonProperty("earliest_offset") Long earliestOffset,
    @JsonProperty("latest_offset") Long latestOffset,
    @JsonProperty("partition") Integer partition,
    @JsonProperty("size") Long size
) {
}
