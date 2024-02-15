/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic Kafka record to be used in place of {@link ProducerRecord} and {@link ConsumerRecord}/
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface KafkaRecord<K, V> {

    /**
     * @return The topic this record should be sent to or is received from.
     */
    String topic();

    /**
     * @return The key (or {@code null} if no key is specified).
     */
    K key();

    /**
     * @return The value (or {@code null} if no key is specified).
     */
    V value();

    /**
     * @return The timestamp of the record, in milliseconds since epoch (or {@code -1} if no
     * timestamp is specified).
     */
    Long timestamp();

    /**
     * @return The type of returned {@link #timestamp()} (or {@code null} if timestamp-type is
     * unknown).
     */
    TimestampType timestampType();

    /**
     * @return The partition to which the record should be sent or is received from (or {@code null}
     * if partition is unknown).
     */
    Integer partition();

    /**
     * @return The offset of this record in the corresponding Kafka partition
     */
    Long offset();

    /**
     * @return The headers included in the record.
     */
    Headers headers();

    /**
     * @return The corresponding {@link ProducerRecord} for this record.
     */
    default ProducerRecord<K, V> toProducerRecord() {
        return new ProducerRecord<>(
                topic(),
                partition(),
                timestamp(),
                key(),
                value(),
                headers()
        );
    }

    /**
     * @return a builder from this record *
     */
    default Builder<K, V> toBuilder() {
        return new Builder<>(this);
    }

    /**
     * @return an empty kafka record *
     */
    static <K, V> KafkaRecord<K, V> empty() {
        return new ClientKafkaRecord<>();
    }

    default <KK> KafkaRecord<KK, V> mapKey(final Function<K, KK> fn) {
        return KafkaRecord.<KK, V>builder()
                .key(fn.apply(key()))
                .value(value())
                .headers(headers())
                .topic(topic())
                .timestamp(timestamp())
                .offset(offset())
                .partition(partition())
                .timestampType(timestampType())
                .build();
    }

    default <VV> KafkaRecord<K, VV> mapValue(final Function<V, VV> fn) {
        return KafkaRecord.<K, VV>builder()
                .key(key())
                .value(fn.apply(value()))
                .headers(headers())
                .topic(topic())
                .timestamp(timestamp())
                .offset(offset())
                .partition(partition())
                .timestampType(timestampType())
                .build();
    }

    /**
     * Creates a new {@link KafkaRecord} from the specified {@link ProducerRecord}.
     *
     * @param record the {@link ProducerRecord} from which to create the new {@link KafkaRecord}.
     * @param <K>    the record-key type.
     * @param <V>    the record-value type.
     * @return a new {@link ProducerRecord}.
     */
    static <K, V> KafkaRecord<K, V> of(final ProducerRecord<K, V> record) {
        return new ClientKafkaRecord<>(
                record.topic(),
                record.partition(),
                record.timestamp(),
                record.key(),
                record.value(),
                new RecordHeaders(record.headers()));
    }

    /**
     * Creates a new {@link KafkaRecord} from the specified {@link ConsumerRecord}.
     *
     * @param record the {@link ConsumerRecord} from which to create the new {@link KafkaRecord}.
     * @param <K>    the record-key type.
     * @param <V>    the record-value type.
     * @return a new {@link ConsumerRecord}.
     */
    static <K, V> KafkaRecord<K, V> of(final ConsumerRecord<K, V> record) {
        return new ClientKafkaRecord<>(
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp(),
                record.timestampType(),
                record.key(),
                record.value(),
                new RecordHeaders(record.headers()));
    }

    static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    class Builder<K, V> {
        private KafkaRecord<K, V> internal;

        public Builder() {
            this(new ClientKafkaRecord<>());
        }

        public Builder(final KafkaRecord<K, V> base) {
            this.internal = base;
        }

        public Builder<K, V> topic(@NotNull final String topic) {
            this.internal =
                    new ClientKafkaRecord<>(
                            topic,
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> key(@Nullable final K key) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            key,
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> value(@Nullable final V value) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            value,
                            internal.headers());
            return this;
        }

        public Builder<K, V> partition(final Integer partition) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            partition,
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> offset(final Long offset) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            offset,
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> timestamp(final Long timestamp) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            timestamp,
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> timestampType(final TimestampType timestampType) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            timestampType,
                            internal.key(),
                            internal.value(),
                            internal.headers());
            return this;
        }

        public Builder<K, V> header(final String key, final String value) {
            return header(key, value == null ? null : value.getBytes(StandardCharsets.UTF_8));
        }

        public Builder<K, V> header(final String key, final byte[] value) {
            RecordHeaders headers = new RecordHeaders(internal.headers());
            headers.add(key, value);

            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            headers);
            return this;
        }

        public Builder<K, V> headers(final Headers headers) {
            this.internal =
                    new ClientKafkaRecord<>(
                            internal.topic(),
                            internal.partition(),
                            internal.offset(),
                            internal.timestamp(),
                            internal.timestampType(),
                            internal.key(),
                            internal.value(),
                            headers);
            return this;
        }

        public KafkaRecord<K, V> build() {
            return internal;
        }
    }

    /**
     * Represents a {@link KafkaRecord}.
     */
    final class ClientKafkaRecord<K, V> implements KafkaRecord<K, V> {

        public static final Long NO_TIMESTAMP = null;
        public static final TimestampType NO_TIMESTAMP_TYPE = null;
        public static final long NO_OFFSET = -1L;
        private final String topic;
        private final Integer partition;
        private final Long offset;
        private final Long timestamp;
        private final TimestampType timestampType;
        private final K key;
        private final V value;
        private final Headers headers;

        /**
         * Creates a new {@link ClientKafkaRecord} instance.
         *
         * @param topic         The topic this record should be sent to or is received from.
         * @param partition     The partition to which the record should be sent or is received from (or
         *                      {@code null} if partition is unknown).
         * @param offset        The offset of this record in the corresponding Kafka partition.
         * @param timestamp     The timestamp of the record, in milliseconds since epoch (or {@code -1}
         *                      if no timestamp is specified).
         * @param timestampType The type of returned {@link #timestamp()} (or {@code null} if
         *                      timestamp-type is unknown).
         * @param key           The key (or {@code null} if no key is specified).
         * @param value         The value (or {@code null} if no value is specified).
         * @param headers       The headers included in the record.
         */
        public ClientKafkaRecord(
                String topic,
                Integer partition,
                Long offset,
                Long timestamp,
                TimestampType timestampType,
                K key,
                V value,
                Headers headers) {
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.timestamp = timestamp;
            this.timestampType = timestampType;
            this.key = key;
            this.value = value;
            this.headers = headers;
        }

        private ClientKafkaRecord() {
            this(null, null, null, null);
        }

        public ClientKafkaRecord(
                @Nullable final String topic, @Nullable final K key, @Nullable final V value) {
            this(
                    topic,
                    null,
                    NO_OFFSET,
                    NO_TIMESTAMP,
                    NO_TIMESTAMP_TYPE,
                    key,
                    value,
                    new RecordHeaders());
        }

        public ClientKafkaRecord(
                @Nullable final String topic,
                @Nullable final Integer partition,
                @Nullable final K key,
                @Nullable final V value) {
            this(
                    topic,
                    partition,
                    NO_OFFSET,
                    NO_TIMESTAMP,
                    NO_TIMESTAMP_TYPE,
                    key,
                    value,
                    new RecordHeaders());
        }

        public ClientKafkaRecord(
                @Nullable final String topic,
                @Nullable final Integer partition,
                @Nullable final K key,
                @Nullable final V value,
                @Nullable final Headers headers) {
            this(topic, partition, NO_OFFSET, NO_TIMESTAMP, NO_TIMESTAMP_TYPE, key, value, headers);
        }

        public ClientKafkaRecord(
                @Nullable final String topic,
                @Nullable final Integer partition,
                @Nullable final Long timestamp,
                @Nullable final K key,
                @Nullable final V value,
                @Nullable final Headers headers) {
            this(topic, partition, NO_OFFSET, timestamp, NO_TIMESTAMP_TYPE, key, value, headers);
        }

        public String topic() {
            return topic;
        }

        public Integer partition() {
            return partition;
        }

        public Long offset() {
            return offset;
        }

        public Long timestamp() {
            return timestamp;
        }

        public TimestampType timestampType() {
            return timestampType;
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }

        public Headers headers() {
            return headers;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ClientKafkaRecord) obj;

            return Objects.equals(this.topic, that.topic)
                    && Objects.equals(this.partition, that.partition)
                    && Objects.equals(this.offset, that.offset)
                    && Objects.equals(this.timestamp, that.timestamp)
                    && Objects.equals(this.timestampType, that.timestampType)
                    && Objects.equals(this.key, that.key)
                    && Objects.equals(this.value, that.value)
                    && Objects.equals(this.headers, that.headers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    topic, partition, offset, timestamp, timestampType, key, value, headers);
        }

        @Override
        public String toString() {
            return "ClientKafkaRecord["
                    + "topic="
                    + topic
                    + ", "
                    + "partition="
                    + partition
                    + ", "
                    + "offset="
                    + offset
                    + ", "
                    + "timestamp="
                    + timestamp
                    + ", "
                    + "timestampType="
                    + timestampType
                    + ", "
                    + "key="
                    + key
                    + ", "
                    + "value="
                    + value
                    + ", "
                    + "headers="
                    + headers
                    + ']';
        }
    }
}
