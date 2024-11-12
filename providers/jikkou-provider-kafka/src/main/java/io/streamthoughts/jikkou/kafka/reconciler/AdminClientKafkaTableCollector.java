/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTableRecordList;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerRecordCallback;
import io.streamthoughts.jikkou.kafka.internals.consumer.KafkaLogToEndConsumer;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTableRecord.class)
public final class AdminClientKafkaTableCollector extends ContextualExtension implements Collector<V1KafkaTableRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTableCollector.class);

    private ConsumerFactory<byte[], byte[]> consumerFactory;

    private AdminClientFactory adminClientFactory;

    /**
     * Creates a new {@link AdminClientKafkaTableCollector} instance.
     */
    public AdminClientKafkaTableCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTableCollector} instance.
     *
     * @param consumerFactory the Consumer factory.
     */
    public AdminClientKafkaTableCollector(final @Nullable ConsumerFactory<byte[], byte[]> consumerFactory,
                                          final @Nullable AdminClientFactory adminClientFactory) {
        this.consumerFactory = consumerFactory;
        this.adminClientFactory = adminClientFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);

        if (consumerFactory == null) {
            consumerFactory = context.<KafkaExtensionProvider>provider().newConsumerFactory();
        }

        if (adminClientFactory == null) {
            adminClientFactory = context.<KafkaExtensionProvider>provider().newAdminClientFactory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceList<V1KafkaTableRecord> listAll(@NotNull final Configuration configuration,
                                                    @NotNull final Selector selector) {

        final String topicName = TopicConfig.TOPIC_NAME.get(configuration);
        LOG.debug("Checking if kafka topic {} is compacted", topicName);
        try (AdminClientContext client = new AdminClientContext(adminClientFactory)) {
            boolean isCompacted = client.isTopicCleanupPolicyCompact(topicName, false);
            if (!isCompacted) {
                throw new JikkouRuntimeException(
                    String.format(
                        "Cannot list records from non compacted topic '%s'. Topic must be configured with: %s=%s",
                        topicName,
                        org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG,
                        org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT
                    )
                );
            }
        }

        LOG.debug("Listing all records from kafka topic {}", topicName);
        KafkaLogToEndConsumer<byte[], byte[]> consumer = new KafkaLogToEndConsumer<>(consumerFactory);

        InternalConsumerRecordCallback callback = new InternalConsumerRecordCallback(
            TopicConfig.KEY_TYPE.get(configuration),
            TopicConfig.VALUE_TYPE.get(configuration),
            TopicConfig.SKIP_MESSAGE_ON_ERROR.get(configuration)
        );

        consumer.readTopicToEnd(topicName, callback);

        List<V1KafkaTableRecord> items = callback.allRecords()
            .stream()
            .filter(item -> {
                // record with key null must be filtered out
                DataValue key = item.getSpec().getKey();
                return key != null && !key.data().isNull();
            })
            .filter(selector::apply)
            .collect(Collectors.toList());
        return new V1KafkaTableRecordList.Builder().withItems(items).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            TopicConfig.TOPIC_NAME,
            TopicConfig.KEY_TYPE,
            TopicConfig.VALUE_TYPE,
            TopicConfig.SKIP_MESSAGE_ON_ERROR
        );
    }

    public static class InternalConsumerRecordCallback implements ConsumerRecordCallback<byte[], byte[]> {

        private final Map<DataHandle, V1KafkaTableRecord> accumulator;
        private final DataType keyType;
        private final DataType valueType;
        private final boolean skipMessageOnError;

        public InternalConsumerRecordCallback(DataType keyType,
                                              DataType valueType,
                                              boolean skipMessageOnError) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.skipMessageOnError = skipMessageOnError;
            this.accumulator = new LinkedHashMap<>();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public void accept(KafkaRecord<byte[], byte[]> record) {
            LOG.debug(
                "Consuming from kafka from {}-{} at offset {}",
                record.topic(),
                record.partition(),
                record.offset()
            );

            if (record.key() == null) {
                LOG.debug("Skipping record with key 'null' from {}-{} at offset {}",
                    record.topic(),
                    record.partition(),
                    record.offset()
                );
                return;
            }

            final DataHandle key = deserialize(record, record.key(), keyType, true)
                .get();

            if (record.value() == null) {
                LOG.debug("Detecting tombstone record for key '{}' from {}-{} at offset {}",
                    key,
                    record.topic(),
                    record.partition(),
                    record.offset()
                );
                accumulator.remove(key);
                return;
            }

            final DataHandle value = deserialize(record, record.value(), valueType, false)
                .get();

            List<KafkaRecordHeader> headers = StreamSupport
                .stream(record.headers().spliterator(), false)
                .map(h -> new KafkaRecordHeader(h.key(), new String(h.value(), StandardCharsets.UTF_8)))
                .toList();

            V1KafkaTableRecord data = V1KafkaTableRecord
                .builder()
                .withMetadata(ObjectMeta
                    .builder()
                    .withAnnotation("kafka.jikkou.io/record-partition", record.partition())
                    .withAnnotation("kafka.jikkou.io/record-offset", record.offset())
                    .withAnnotation("kafka.jikkou.io/record-timestamp", record.timestamp())
                    .build()
                )
                .withSpec(V1KafkaTableRecordSpec
                    .builder()
                    .withTopic(record.topic())
                    .withKey(new DataValue(
                        keyType,
                        key
                    ))
                    .withValue(new DataValue(
                        valueType,
                        value
                    ))
                    .withHeaders(headers)
                    .build()
                )
                .build();
            accumulator.put(key, data);
        }

        private Optional<DataHandle> deserialize(
            final KafkaRecord<byte[], byte[]> record,
            final byte[] data,
            final DataType format,
            final boolean isKey) {
            try {
                ByteBuffer keyByteBuffer = Optional.ofNullable(data)
                    .map(ByteBuffer::wrap).orElse(null);

                return format.getDataSerde()
                    .deserialize(record.topic(), keyByteBuffer, Map.of(), isKey)
                    .or(() -> Optional.of(DataHandle.NULL));

            } catch (Exception e) {
                if (skipMessageOnError) {
                    LOG.info("Skip message from {}-{} at offset {}. Error while deserializing record: {}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        e.getLocalizedMessage()
                    );
                } else {
                    throw new JikkouRuntimeException(String.format(
                        "Error while deserializing record from from %s-%d at offset %d. %s",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        e.getLocalizedMessage()
                    ), e);
                }
            }

            return Optional.empty();
        }

        public List<V1KafkaTableRecord> allRecords() {
            return new ArrayList<>(accumulator.values());
        }
    }
}
