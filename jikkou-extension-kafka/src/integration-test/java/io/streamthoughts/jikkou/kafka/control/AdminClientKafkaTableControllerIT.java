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
package io.streamthoughts.jikkou.kafka.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.api.DefaultApi;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
import io.streamthoughts.jikkou.kafka.change.RecordChange;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.model.DataFormat;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.KafkaRecordData;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminClientKafkaTableControllerIT extends AbstractKafkaIntegrationTest {

    static final String CLASSPATH_RESOURCE_TOPICS = "datasets/resource-kafka-record-string-value.yaml";
    static final String TEST_TOPIC_COMPACTED = "topic-compacted";
    static final String BEFORE_RECORD_VALUE = """
            {
               "favorite_color": "blue"
            }
             """;
    static final String BEFORE_AFTER_VALUE = """
            {
               "favorite_color": "red"
            }
             """;
    public static final KafkaRecordHeader KAFKA_RECORD_HEADER = new KafkaRecordHeader("content-type", "application/json");

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));
    private volatile JikkouApi api;

    @BeforeAll
    public static void beforeAll() {
        ResourceDeserializer.registerKind(V1KafkaTableRecord.class);
    }

    @BeforeEach
    public void setUp() {
        var controller = new AdminClientKafkaTableController();
        controller.configure(KafkaClientConfiguration.PRODUCER_CLIENT_CONFIG.asConfiguration(clientConfig()));

        var collector = new AdminClientKafkaTableCollector();
        controller.configure(KafkaClientConfiguration.CONSUMER_CLIENT_CONFIG.asConfiguration(clientConfig()));

        api = DefaultApi.builder()
                .withController(controller)
                .withCollector(collector)
                .build();

        createTopic(TEST_TOPIC_COMPACTED, Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));
    }

    @Test
    public void shouldApplyReconciliationForCreateMode() throws JsonProcessingException {
        // GIVEN
        var resources = loader
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.builder()
                .dryRun(false)
                .build();

        // WHEN
        List<Change> actual = api.apply(resources, ReconciliationMode.APPLY_ALL, context)
                .stream()
                .map(ChangeResult::data)
                .map(HasMetadataChange::getChange)
                .toList();

        // THEN

        JsonNode value = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_AFTER_VALUE);
        RecordChange expected = RecordChange.builder()
                .withChangeType(ChangeType.ADD)
                .withTopic(TEST_TOPIC_COMPACTED)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.JSON)
                .withRecord(ValueChange.withAfterValue(
                        KafkaRecordData
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(DataHandle.of("test"))
                                .withValue(new DataHandle(value))
                                .build()
                ))
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }

    @Test
    public void shouldApplyReconciliationForUpdateMode() throws JsonProcessingException, ExecutionException, InterruptedException {
        // GIVEN
        try (var producer = new KafkaProducer<>(clientConfig(), new StringSerializer(), new StringSerializer())) {
            var record = KafkaRecord.<String, String>builder()
                    .topic(TEST_TOPIC_COMPACTED)
                    .key("test")
                    .value(BEFORE_RECORD_VALUE)
                    .header("content-type", "application/json")
                    .build();
            producer.send(record.toProducerRecord()).get();
        }

        var resources = loader
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.builder()
                .dryRun(false)
                .build();

        // WHEN
        List<Change> actual = api.apply(resources, ReconciliationMode.APPLY_ALL, context)
                .stream()
                .map(ChangeResult::data)
                .map(HasMetadataChange::getChange)
                .toList();

        // THEN

        JsonNode beforeValue = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_RECORD_VALUE);
        JsonNode afterValue = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_AFTER_VALUE);
        RecordChange expected = RecordChange.builder()
                .withChangeType(ChangeType.UPDATE)
                .withTopic(TEST_TOPIC_COMPACTED)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.JSON)
                .withRecord(ValueChange.with(
                        KafkaRecordData
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(DataHandle.of("test"))
                                .withValue(new DataHandle(beforeValue))
                                .build(),
                        KafkaRecordData
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(DataHandle.of("test"))
                                .withValue(new DataHandle(afterValue))
                                .build()
                ))
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }
}