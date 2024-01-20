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
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTableRecordList;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.admin.DefaultAdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerRecordCallback;
import io.streamthoughts.jikkou.kafka.internals.consumer.DefaultConsumerFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.KafkaLogToEndConsumer;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTableRecord.class)
@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = AdminClientKafkaTableCollector.TOPIC_NAME_CONFIG,
                        description = "The topic name to consume on.",
                        type = String.class,
                        required = true
                ),
                @ExtensionOptionSpec(
                        name = AdminClientKafkaTableCollector.KEY_TYPE_CONFIG,
                        description = "The record key type. Valid values: ${COMPLETION-CANDIDATES}.",
                        type = DataType.class,
                        required = true
                ),
                @ExtensionOptionSpec(
                        name = AdminClientKafkaTableCollector.VALUE_TYPE_CONFIG,
                        description = "The record value type. Valid values: ${COMPLETION-CANDIDATES}.",
                        type = DataType.class,
                        required = true
                ),
                @ExtensionOptionSpec(
                        name = AdminClientKafkaTableCollector.SKIP_MESSAGE_ON_ERROR_CONFIG,
                        description = "If there is an error when processing a message, skip it instead of halt.",
                        type = Boolean.class,
                        defaultValue = "false",
                        required = false
                )
        }
)

public final class AdminClientKafkaTableCollector extends ContextualExtension implements Collector<V1KafkaTableRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTableCollector.class);
    public static final Map<String, Object> EMPTY_CONFIG = Collections.emptyMap();
    public static final String TOPIC_NAME_CONFIG = "topic-name";
    public static final String KEY_TYPE_CONFIG = "key-type";
    public static final String VALUE_TYPE_CONFIG = "value-type";
    public static final String SKIP_MESSAGE_ON_ERROR_CONFIG = "skip-message-on-error";

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
            Map<String, Object> clientConfig = KafkaClientConfiguration.CONSUMER_CLIENT_CONFIG.get(context.appConfiguration());
            consumerFactory = new DefaultConsumerFactory<byte[], byte[]>(clientConfig)
                    .setKeyDeserializer(new ByteArrayDeserializer())
                    .setValueDeserializer(new ByteArrayDeserializer());
        }

        if (adminClientFactory == null) {
            adminClientFactory = new DefaultAdminClientFactory(() ->
                    KafkaClientConfiguration.ADMIN_CLIENT_CONFIG.get(context.appConfiguration())
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceListObject<V1KafkaTableRecord> listAll(@NotNull final Configuration configuration,
                                                          @NotNull final Selector selector) {

        //final Config config = new Config(configuration);
        final String topicName = extensionContext().<String>configProperty(TOPIC_NAME_CONFIG).get(configuration);
        LOG.debug("Checking if kafka topic {} is compacted", topicName);
        try (AdminClientContext client = new AdminClientContext(adminClientFactory)) {
            boolean isCompacted = client.isTopicCleanupPolicyCompact(topicName, false);
            if (!isCompacted) {
                throw new JikkouRuntimeException(
                        String.format(
                                "Cannot list records from non compacted topic '%s'. Topic must be configured with: %s=%s",
                                topicName,
                                TopicConfig.CLEANUP_POLICY_CONFIG,
                                TopicConfig.CLEANUP_POLICY_COMPACT
                        )
                );
            }
        }

        LOG.debug("Listing all records from kafka topic {}", topicName);
        KafkaLogToEndConsumer<byte[], byte[]> consumer = new KafkaLogToEndConsumer<>(consumerFactory);

        InternalConsumerRecordCallback callback = new InternalConsumerRecordCallback(
                extensionContext().<DataType>configProperty(KEY_TYPE_CONFIG).get(configuration),
                extensionContext().<DataType>configProperty(VALUE_TYPE_CONFIG).get(configuration),
                extensionContext().<Boolean>configProperty(SKIP_MESSAGE_ON_ERROR_CONFIG).get(configuration)
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
        return new V1KafkaTableRecordList(items);
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
                        .deserialize(record.topic(), keyByteBuffer, EMPTY_CONFIG, isKey)
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
