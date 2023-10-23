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
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceDeserializer;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
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
        Configuration configuration = KafkaClientConfiguration.PRODUCER_CLIENT_CONFIG.asConfiguration(clientConfig());

        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        api = DefaultApi.builder(new DefaultExtensionFactory(registry, configuration))
                .register(AdminClientKafkaTableController.class, AdminClientKafkaTableController::new)
                .register(AdminClientKafkaTableCollector.class, AdminClientKafkaTableCollector::new)
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
        KafkaTableRecordChange expected = KafkaTableRecordChange.builder()
                .withChangeType(ChangeType.ADD)
                .withTopic(TEST_TOPIC_COMPACTED)
                .withRecord(ValueChange.withAfterValue(V1KafkaTableRecordSpec
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                                .withValue(new DataValue(DataType.JSON, new DataHandle(value)))
                                .build()
                        )
                )
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
        KafkaTableRecordChange expected = KafkaTableRecordChange.builder()
                .withChangeType(ChangeType.UPDATE)
                .withTopic(TEST_TOPIC_COMPACTED)
                .withRecord(ValueChange.with(
                        V1KafkaTableRecordSpec
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                                .withValue(new DataValue(DataType.JSON, new DataHandle(beforeValue)))
                                .build(),
                        V1KafkaTableRecordSpec
                                .builder()
                                .withHeader(KAFKA_RECORD_HEADER)
                                .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                                .withValue(new DataValue(DataType.JSON, new DataHandle(afterValue)))
                                .build()
                ))
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }
}