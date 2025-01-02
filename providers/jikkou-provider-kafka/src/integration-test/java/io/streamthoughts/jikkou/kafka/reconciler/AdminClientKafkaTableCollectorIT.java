/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdminClientKafkaTableCollectorIT extends BaseExtensionProviderIT {

    static final String TEST_TOPIC_NAME = "test";

    public static final String TEST_RECORD_KEY = "key";
    static final KafkaRecord<String, String> RECORD_KEY_V1 = KafkaRecord.<String, String>builder()
        .topic(TEST_TOPIC_NAME)
        .key(TEST_RECORD_KEY)
        .value("v1")
        .header("k", "v")
        .build();

    static final KafkaRecord<String, String> RECORD_KEY_V2 = KafkaRecord.<String, String>builder()
        .topic(TEST_TOPIC_NAME)
        .key(TEST_RECORD_KEY)
        .value("v2")
        .header("k", "v")
        .build();

    @Test
    void shouldListRecordForCompactTopic() throws ExecutionException, InterruptedException {
        // Given
        createTopic(TEST_TOPIC_NAME, Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));

        RecordMetadata metadata;
        try (var producer = new KafkaProducer<>(clientConfig(), new StringSerializer(), new StringSerializer())) {
            producer.send(RECORD_KEY_V1.toProducerRecord()).get();
            metadata = producer.send(RECORD_KEY_V2.toProducerRecord()).get();
        }

        // When
        Configuration configuration = Configuration.of(
            AdminClientKafkaTableCollector.TOPIC_NAME_CONFIG, TEST_TOPIC_NAME,
            AdminClientKafkaTableCollector.KEY_TYPE_CONFIG, DataType.STRING.name(),
            AdminClientKafkaTableCollector.VALUE_TYPE_CONFIG, DataType.STRING.name());

        ResourceList<V1KafkaTableRecord> list = api.listResources(
            V1KafkaTableRecord.class,
            Selectors.NO_SELECTOR,
            configuration
        );

        // Then
        var expected = V1KafkaTableRecord
            .builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation("kafka.jikkou.io/record-partition", metadata.partition())
                .withAnnotation("kafka.jikkou.io/record-offset", metadata.offset())
                .withAnnotation("kafka.jikkou.io/record-timestamp", metadata.timestamp())
                .build()
            )
            .withSpec(V1KafkaTableRecordSpec
                .builder()
                .withTopic(TEST_TOPIC_NAME)
                .withHeader(new KafkaRecordHeader("k", "v"))
                .withKey(new DataValue(DataType.STRING, DataHandle.ofString(RECORD_KEY_V2.key())))
                .withValue(new DataValue(DataType.STRING, DataHandle.ofString(RECORD_KEY_V2.value())))
                .build()
            )
            .build();
        Assertions.assertEquals(List.of(expected), list.getItems());

    }

    @Test
    void shouldThrowExceptionWhenListingRecordsForNonCompactTopic() {
        // Given
        createTopic(TEST_TOPIC_NAME, Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE));

        // When
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            Configuration configuration = Configuration.of(
                AdminClientKafkaTableCollector.TOPIC_NAME_CONFIG, TEST_TOPIC_NAME,
                AdminClientKafkaTableCollector.KEY_TYPE_CONFIG, DataType.STRING.name(),
                AdminClientKafkaTableCollector.VALUE_TYPE_CONFIG, DataType.STRING.name());

            api.listResources(
                V1KafkaTableRecord.class,
                Selectors.NO_SELECTOR,
                configuration
            );
        });
    }
}