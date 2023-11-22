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
package io.streamthoughts.jikkou.kafka.reconcilier;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
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

public class AdminClientKafkaTableCollectorIT extends AbstractKafkaIntegrationTest {

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
        Configuration config = KafkaClientConfiguration.CONSUMER_CLIENT_CONFIG.asConfiguration(clientConfig());
        AdminClientKafkaTableCollector collector = new AdminClientKafkaTableCollector(config);
        ResourceListObject<V1KafkaTableRecord> list = collector.listAll(Configuration.of(
                AdminClientKafkaTableCollector.Config.TOPIC_CONFIG_NAME, TEST_TOPIC_NAME,
                AdminClientKafkaTableCollector.Config.KEY_TYPE_CONFIG_NAME, DataType.STRING.name(),
                AdminClientKafkaTableCollector.Config.VALUE_TYPE_CONFIG_NAME, DataType.STRING.name())
        );
        // Then
        var expected = V1KafkaTableRecord
                .builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC_NAME)
                        .withAnnotation("kafka.jikkou.io/record-partition", metadata.partition())
                        .withAnnotation("kafka.jikkou.io/record-offset", metadata.offset())
                        .withAnnotation("kafka.jikkou.io/record-timestamp", metadata.timestamp())
                        .build()
                )
                .withSpec(V1KafkaTableRecordSpec
                        .builder()
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
        Configuration config = KafkaClientConfiguration.CONSUMER_CLIENT_CONFIG.asConfiguration(clientConfig());
        AdminClientKafkaTableCollector collector = new AdminClientKafkaTableCollector(config);
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            collector.listAll(Configuration.of(
                    AdminClientKafkaTableCollector.Config.TOPIC_CONFIG_NAME, TEST_TOPIC_NAME,
                    AdminClientKafkaTableCollector.Config.KEY_TYPE_CONFIG_NAME, DataType.STRING.name(),
                    AdminClientKafkaTableCollector.Config.VALUE_TYPE_CONFIG_NAME, DataType.STRING.name())
            );
        });
    }
}