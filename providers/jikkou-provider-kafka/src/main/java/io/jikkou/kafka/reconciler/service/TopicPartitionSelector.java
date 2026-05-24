/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import java.util.Objects;
import java.util.OptionalInt;
import org.jetbrains.annotations.NotNull;

/**
 * A selector for either a whole topic (all partitions) or a single topic-partition.
 * Parses the {@code topic} or {@code topic:partition} syntax used by
 * {@code kafka-consumer-groups.sh}.
 *
 * @param topic     the topic name, never {@code null} or empty.
 * @param partition the partition; {@link OptionalInt#empty()} means "all partitions of the topic".
 */
public record TopicPartitionSelector(@NotNull String topic, @NotNull OptionalInt partition) {

    public TopicPartitionSelector {
        Objects.requireNonNull(topic, "topic cannot be null");
        Objects.requireNonNull(partition, "partition cannot be null");
        if (topic.isEmpty()) {
            throw new IllegalArgumentException("topic cannot be empty");
        }
    }

    /**
     * Parses a {@code topic} or {@code topic:partition} string.
     *
     * @param raw the raw input.
     * @return the parsed selector.
     * @throws IllegalArgumentException if the input is null, empty, or malformed.
     */
    public static TopicPartitionSelector parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("topic selector cannot be null or empty");
        }
        int idx = raw.indexOf(':');
        if (idx < 0) {
            return new TopicPartitionSelector(raw, OptionalInt.empty());
        }
        // Reject more than one ':' (kafka topic names never contain ':').
        if (raw.indexOf(':', idx + 1) >= 0) {
            throw new IllegalArgumentException(
                "Invalid topic selector '" + raw + "': expected 'topic' or 'topic:partition'");
        }
        String topic = raw.substring(0, idx);
        String partitionPart = raw.substring(idx + 1);
        if (topic.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid topic selector '" + raw + "': topic name cannot be empty");
        }
        if (partitionPart.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid topic selector '" + raw + "': partition cannot be empty");
        }
        final int partition;
        try {
            partition = Integer.parseInt(partitionPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid topic selector '" + raw + "': partition '" + partitionPart + "' is not an integer");
        }
        if (partition < 0) {
            throw new IllegalArgumentException(
                "Invalid topic selector '" + raw + "': partition must be non-negative");
        }
        return new TopicPartitionSelector(topic, OptionalInt.of(partition));
    }

    public boolean isAllPartitions() {
        return partition.isEmpty();
    }
}
