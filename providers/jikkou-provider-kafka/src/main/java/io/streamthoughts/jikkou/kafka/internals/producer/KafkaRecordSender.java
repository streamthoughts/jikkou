/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.producer;

import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KafkaRecordSender<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaRecordSender.class);

    private final Producer<K, V> producer;

    /**
     * Creates a new {@link KafkaRecordSender} instance.
     *
     * @param producer   the Producer. Must not be {@code null}.
     */
    public KafkaRecordSender(@NotNull final Producer<K, V> producer) {
        this.producer = Objects.requireNonNull(producer, "producer must not be null");
    }

    public List<CompletableFuture<ProducerRequestResult<K, V>>> send(
            @NotNull final Collection<KafkaRecord<K, V>> records) {
        return doSend(producer, records);
    }

    public CompletableFuture<ProducerRequestResult<K, V>> send(
            @NotNull final KafkaRecord<K, V> record) {
        return doSend(producer, record);
    }

    private static <K, V> List<CompletableFuture<ProducerRequestResult<K, V>>> doSend(
            final Producer<K, V> producer,
            final Collection<KafkaRecord<K, V>> records) {

        return records
                .stream()
                .map(record -> doSend(producer, record))
                .toList();
    }

    private static <K, V> CompletableFuture<ProducerRequestResult<K, V>> doSend(
            final Producer<K, V> producer,
            final KafkaRecord<K, V> record) {
        LOG.debug("Sending record to topic {}", record.topic());

        try {
            CompletableFuture<ProducerRequestResult<K, V>> future = new CompletableFuture<>();
            producer.send(record.toProducerRecord(), buildCallback(future, record));
            return future;
        } catch (KafkaException e) {
            return CompletableFuture.completedFuture(new ProducerRequestResult<>(record, e));
        }
    }

    private static <K, V> Callback buildCallback(CompletableFuture<ProducerRequestResult<K, V>> future,
                                                 KafkaRecord<K, V> record) {
        return new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                ProducerRequestResult<K, V> result;
                if (exception == null) {
                    LOG.debug(
                            "Record was successfully sent to kafka topic {}-{} ",
                            metadata.topic(),
                            metadata.partition()
                    );

                    result = new ProducerRequestResult<>(
                            record,
                            metadata.partition(),
                            metadata.offset(),
                            metadata.timestamp()
                    );
                } else {
                    LOG.warn("Failed to send record into kafka topic {}",
                            metadata.topic(),
                            exception);
                    result = new ProducerRequestResult<>(record, exception);
                }
                future.complete(result);
            }
        };
    }
}
