/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.producer;

import io.streamthoughts.jikkou.common.memory.OpaqueMemoryResource;
import io.streamthoughts.jikkou.common.memory.ResourceDisposer;
import io.streamthoughts.jikkou.common.memory.SharedResources;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultProducerFactory<K, V> implements ProducerFactory<K, V>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProducerFactory.class);

    private final Supplier<Map<String, Object>> configSupplier;
    private Serializer<K> keySerializer;
    private Serializer<V> valueSerializer;
    private Duration closeTimeout = Duration.ofSeconds(30);
    /* share producer instance **/
    private final SharedResources resources = new SharedResources();
    private final List<Object> leaseHolders = new ArrayList<>();

    /**
     * Creates a new {@link DefaultProducerFactory} instance.
     *
     * @param config the Producer client properties.
     */
    public DefaultProducerFactory(@NotNull Map<String, Object> config) {
        Map<String, Object> immutable = Collections.unmodifiableMap(config);
        this.configSupplier = () -> immutable;
    }

    /**
     * Creates a new {@link DefaultProducerFactory} instance.
     *
     * @param config          the Producer client properties.
     * @param keySerializer   the Serializer for the record-key.
     * @param valueSerializer the Serializer for the record-value.
     */
    public DefaultProducerFactory(@NotNull Map<String, Object> config,
                                  @Nullable Serializer<K> keySerializer,
                                  @Nullable Serializer<V> valueSerializer) {
        Map<String, Object> immutable = Collections.unmodifiableMap(config);
        this.configSupplier = () -> immutable;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    /**
     * Creates a new {@link DefaultProducerFactory} instance.
     *
     * @param configSupplier  the Producer client properties supplier
     * @param keySerializer   the Serializer for the record-key.
     * @param valueSerializer the Serializer for the record-value.
     */
    public DefaultProducerFactory(@NotNull Supplier<Map<String, Object>> configSupplier,
                                  @Nullable Serializer<K> keySerializer,
                                  @Nullable Serializer<V> valueSerializer) {
        this.configSupplier = configSupplier;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    public DefaultProducerFactory<K, V> setKeySerializer(Serializer<K> keySerializer) {
        this.keySerializer = keySerializer;
        return this;
    }

    public DefaultProducerFactory<K, V> setValueSerializer(Serializer<V> valueSerializer) {
        this.valueSerializer = valueSerializer;
        return this;
    }

    public DefaultProducerFactory<K, V> setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Producer<K, V> createProducer() {
        final Object leaseHolder = new Object();
        Producer<K, V> producer = resources.getOrCreateSharedResource(
                "kafka-producer",
                this::createKafkaProducer,
                leaseHolder
        );

        ResourceDisposer<Exception> disposer = createResourceDisposer(leaseHolder);

        leaseHolders.add(leaseHolder);
        return new OpaqueProducer<>(new OpaqueMemoryResource<>(producer, disposer));
    }

    @NotNull
    private ResourceDisposer<Exception> createResourceDisposer(@NotNull final Object leaseHolder) {
        return () -> resources.release("kafka-producer", leaseHolder, this::closeKafkaProducer);
    }

    @NotNull
    private Producer<K, V> createKafkaProducer() {
        LOG.info("Creating new kafka producer instance.");
        return new KafkaProducer<>(
                loadConfigs(),
                keySerializer,
                valueSerializer
        );
    }

    private void closeKafkaProducer(Producer<K, V> producer) {
        producer.close(closeTimeout);
    }

    private Map<String, Object> loadConfigs() {
        return configSupplier.get();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        ListIterator<Object> iterator = leaseHolders.listIterator();
        while (iterator.hasNext()) {
            Object leaseHolder = iterator.next();
            try {
                createResourceDisposer(leaseHolder).dispose();
            } catch (Throwable e) {
                LOG.error("Error while closing resource", e);
            }
            iterator.remove();
        }
    }

    /**
     * A {@code ProducerDisposer} can be used to dispose a shared resource after it is not used anymore.
     */

    private static class OpaqueProducer<K, V> implements Producer<K, V> {

        private final OpaqueMemoryResource<Producer<K, V>> delegate;

        /**
         * Creates a new {@link OpaqueMemoryResource} instance.
         *
         * @param delegate the delegate Producer.
         */
        public OpaqueProducer(OpaqueMemoryResource<Producer<K, V>> delegate) {
            this.delegate = delegate;
        }

        /**
         * @see KafkaProducer#initTransactions()
         **/
        @Override
        public void initTransactions() {
            delegate.getResourceHandle().initTransactions();
        }

        /**
         * @see KafkaProducer#beginTransaction()
         **/
        @Override
        public void beginTransaction() throws ProducerFencedException {
            delegate.getResourceHandle().beginTransaction();
        }

        /**
         * @see KafkaProducer#sendOffsetsToTransaction(Map, String)
         **/
        @Deprecated
        @Override
        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                             String consumerGroupId) throws ProducerFencedException {
            delegate.getResourceHandle().sendOffsetsToTransaction(offsets, consumerGroupId);
        }

        /**
         * @see KafkaProducer#sendOffsetsToTransaction(Map, ConsumerGroupMetadata)
         **/
        @Override
        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                             ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
            delegate.getResourceHandle().sendOffsetsToTransaction(offsets, groupMetadata);
        }

        /**
         * @see KafkaProducer#commitTransaction()
         **/
        @Override
        public void commitTransaction() throws ProducerFencedException {
            delegate.getResourceHandle().commitTransaction();
        }

        /**
         * @see KafkaProducer#abortTransaction()
         **/
        @Override
        public void abortTransaction() throws ProducerFencedException {
            delegate.getResourceHandle().abortTransaction();
        }

        /**
         * @see KafkaProducer#send(ProducerRecord)
         **/
        @Override
        public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
            return delegate.getResourceHandle().send(record);
        }

        /**
         * @see KafkaProducer#send(ProducerRecord, Callback)
         **/
        @Override
        public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
            return delegate.getResourceHandle().send(record, callback);
        }

        /**
         * @see KafkaProducer#flush()
         **/
        @Override
        public void flush() {
            delegate.getResourceHandle().flush();
        }

        /**
         * @see KafkaProducer#partitionsFor(String)
         **/
        @Override
        public List<PartitionInfo> partitionsFor(String topic) {
            return delegate.getResourceHandle().partitionsFor(topic);
        }

        /**
         * @see KafkaProducer#metrics()
         **/
        @Override
        public Map<MetricName, ? extends Metric> metrics() {
            return delegate.getResourceHandle().metrics();
        }

        /**
         * @see KafkaProducer#close()
         **/
        @Override
        public void close() {
            close(null);
        }

        /**
         * @see KafkaProducer#close(Duration)
         **/
        @Override
        public void close(Duration timeout) {
            try {
                delegate.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
