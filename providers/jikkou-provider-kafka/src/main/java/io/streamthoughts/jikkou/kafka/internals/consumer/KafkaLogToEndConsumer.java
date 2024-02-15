/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.consumer;

import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple consumer that can be used to consume a topic from the beginning to the end of all partition.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class KafkaLogToEndConsumer<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaLogToEndConsumer.class);

    private final ConsumerFactory<K, V> consumerFactory;

    /**
     * Creates a new {@link KafkaLogToEndConsumer} instance.
     * @param consumerFactory   the Consumer factory.
     */
    public KafkaLogToEndConsumer(@NotNull final ConsumerFactory<K, V>  consumerFactory) {
        this.consumerFactory = Objects.requireNonNull(consumerFactory, "consumer must not be null");
    }

    public void readTopicToEnd(@NotNull final String topic,
                               @NotNull final ConsumerRecordCallback<K, V> callback) {
        try (Consumer<K, V> consumer = consumerFactory.createConsumer()) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            List<TopicPartition> partitions = partitionInfos
                    .stream()
                    .map(it -> new TopicPartition(it.topic(), it.partition()))
                    .toList();
            // Manually assign to all topic partitions;
            consumer.assign(partitions);

            // Always consume from the beginning of all partitions. his is necessary to ensure
            // we don't use committed offsets when a 'group.id' is specified.
            consumer.seekToBeginning(partitions);
            readToPartitionEnd(consumer, callback);
        }
    }

    private void readToPartitionEnd(final Consumer<K, V> consumer,
                                    final ConsumerRecordCallback<K, V> callback) {
        Set<TopicPartition> assignment = consumer.assignment();

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(assignment);
        LOG.info("Reading to end of partitions offsets {}", endOffsets);
        while (!endOffsets.isEmpty()) {
            Iterator<Map.Entry<TopicPartition, Long>> iterator = endOffsets.entrySet().iterator();
            while (iterator.hasNext())  {
                Map.Entry<TopicPartition, Long> entry = iterator.next();
                TopicPartition partition = entry.getKey();
                long nextRecordOffset = consumer.position(partition);
                Long endOffset = entry.getValue();
                // Check if consumer has reached end
                // offset for the current partition
                if (nextRecordOffset < endOffset) {
                    pollOnce(consumer, callback);
                    break;
                } else {
                    LOG.info("Finished read to end partition for {}-{}", partition.topic(), partition.partition());
                    iterator.remove();
                }
            }
        }
    }

    private void pollOnce(final Consumer<K, V> consumer,
                          final ConsumerRecordCallback<K, V> callback) {
        ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
        for (ConsumerRecord<K, V> record : records) {
            callback.accept(KafkaRecord.of(record));
        }
    }
}
