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
package io.streamthoughts.jikkou.kafka.internals.producer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
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

public class DefaultProducerFactory<K, V> implements ProducerFactory<K, V>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProducerFactory.class);

    private final Supplier<Map<String, Object>> configSupplier;
    private Serializer<K> keySerializer;
    private Serializer<V> valueSerializer;

    private Duration closeTimeout = Duration.ofSeconds(30);

    private final ReentrantLock lock = new ReentrantLock();

    /* share producer instance **/
    private LeasedProducer<Producer<K, V>> leasedProducer;

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
     * @param config            the Producer client properties.
     * @param keySerializer     the Serializer for the record-key.
     * @param valueSerializer   the Serializer for the record-value.
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
     * @param configSupplier    the Producer client properties supplier
     * @param keySerializer     the Serializer for the record-key.
     * @param valueSerializer   the Serializer for the record-value.
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
        lock.lock();
        try {
            if (leasedProducer == null) {
                LOG.info("Creating new shared producer instance.");
                Producer<K, V> producer = new KafkaProducer<>(
                        loadConfigs(),
                        keySerializer,
                        valueSerializer
                );
                leasedProducer = new LeasedProducer<>(producer);
            }
            LOG.info("Reclaiming access to shared producer instance.");
            Object leaseHolder = new Object();
            leasedProducer.addLeaseHolder(leaseHolder);
            ProducerDisposer disposer = () -> releaseProducer(leaseHolder);
            return new OpaqueProducerResource<>(leasedProducer.resource(), disposer);
        } finally {
            lock.unlock();
        }

    }

    private Map<String, Object> loadConfigs() {
        return configSupplier.get();
    }

    void releaseProducer(final Object leaseHolder) {
        lock.lock();
        try {
            if (leasedProducer == null) {
                return;
            }
            LOG.info("Releasing access on shared producer instance.");
            if (leasedProducer.removeLeaseHolder(leaseHolder)) {
                closeShareProducer();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        lock.lock();
        try {
            closeShareProducer();
        } finally {
            lock.unlock();
        }
    }

    private void closeShareProducer() {
        if (leasedProducer != null) {
            LOG.info("Closing shared producer instance.");
            leasedProducer.close(closeTimeout);
            leasedProducer = null;
        }
    }

    private final static class LeasedProducer<T extends Producer<?, ?>> {

        private final T resource;
        private final HashSet<Object> leaseHolders = new HashSet<>();
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Creates a new {@link LeasedProducer} instance.
         *
         * @param resource the resource.
         */
        public LeasedProducer(final T resource) {
            this.resource = Objects.requireNonNull(resource, "resource should not be null");
        }

        /**
         * Gets the resource.
         */
        public T resource() {
            return resource;
        }

        /**
         * Adds a new lease to the handle resource.
         *
         * @param leaseHolder the leaseholder object.
         */
        void addLeaseHolder(final Object leaseHolder) {
            leaseHolders.add(leaseHolder);
        }

        /**
         * Removes the given leaseholder.
         *
         * @param leaseHolder the leaseholder object.
         * @return {@code true} is not use anymore, and can be disposed.
         */
        boolean removeLeaseHolder(final Object leaseHolder) {
            leaseHolders.remove(leaseHolder);
            return leaseHolders.isEmpty();
        }

        /**
         * Disposes the resource handled.
         */
        public void close(final Duration duration) {
            if (closed.compareAndSet(false, true)) {
                resource.close(duration);
            }
        }
    }


    /**
     * A {@code ProducerDisposer} can be used to dispose a shared resource after it is not used anymore.
     */

    @FunctionalInterface
    public interface ProducerDisposer {

        /**
         * Release the producer shared resource.
         */
        void dispose();
    }


    private static class OpaqueProducerResource<K, V> implements Producer<K, V> {

        private final Producer<K, V> delegate;

        private final ProducerDisposer disposer;

        private OpaqueProducerResource(Producer<K, V> delegate,
                                       ProducerDisposer disposer) {
            this.delegate = delegate;
            this.disposer = disposer;
        }

        /**
         * @see KafkaProducer#initTransactions()
         **/
        @Override
        public void initTransactions() {
            this.delegate.initTransactions();
        }

        /**
         * @see KafkaProducer#beginTransaction()
         **/
        @Override
        public void beginTransaction() throws ProducerFencedException {
            this.delegate.beginTransaction();
        }

        /**
         * @see KafkaProducer#sendOffsetsToTransaction(Map, String)
         **/
        @Deprecated
        @Override
        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                             String consumerGroupId) throws ProducerFencedException {
            this.delegate.sendOffsetsToTransaction(offsets, consumerGroupId);
        }

        /**
         * @see KafkaProducer#sendOffsetsToTransaction(Map, ConsumerGroupMetadata)
         **/
        @Override
        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                             ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
            this.delegate.sendOffsetsToTransaction(offsets, groupMetadata);
        }

        /**
         * @see KafkaProducer#commitTransaction()
         **/
        @Override
        public void commitTransaction() throws ProducerFencedException {
            this.delegate.commitTransaction();
        }

        /**
         * @see KafkaProducer#abortTransaction()
         **/
        @Override
        public void abortTransaction() throws ProducerFencedException {
            this.delegate.abortTransaction();
        }

        /**
         * @see KafkaProducer#send(ProducerRecord)
         **/
        @Override
        public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
            return this.delegate.send(record);
        }

        /**
         * @see KafkaProducer#send(ProducerRecord, Callback)
         **/
        @Override
        public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
            return this.delegate.send(record, callback);
        }

        /**
         * @see KafkaProducer#flush()
         **/
        @Override
        public void flush() {
            this.delegate.flush();
        }

        /**
         * @see KafkaProducer#partitionsFor(String)
         **/
        @Override
        public List<PartitionInfo> partitionsFor(String topic) {
            return this.delegate.partitionsFor(topic);
        }

        /**
         * @see KafkaProducer#metrics()
         **/
        @Override
        public Map<MetricName, ? extends Metric> metrics() {
            return this.delegate.metrics();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public void close() {
            close(null);
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public void close(Duration timeout) {
            disposer.dispose();
        }
    }
}
