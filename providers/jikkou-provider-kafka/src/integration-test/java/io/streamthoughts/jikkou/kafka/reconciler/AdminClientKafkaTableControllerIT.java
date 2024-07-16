/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
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
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminClientKafkaTableControllerIT extends BaseExtensionProviderIT {

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

    @BeforeEach
    public void setup() {
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
        List<ResourceChange> actual = api.reconcile(resources, ReconciliationMode.FULL, context)
            .results()
            .stream()
            .map(ChangeResult::change)
            .toList();

        // THEN

        JsonNode value = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_AFTER_VALUE);
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaTableRecord.class)
            .withMetadata(new ObjectMeta())
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create("record",
                    V1KafkaTableRecordSpec
                        .builder()
                        .withTopic(TEST_TOPIC_COMPACTED)
                        .withHeader(KAFKA_RECORD_HEADER)
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                        .withValue(new DataValue(DataType.JSON, new DataHandle(value)))
                        .build()
                ))
                .build()
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
        List<ResourceChange> actual = api.reconcile(resources, ReconciliationMode.FULL, context)
            .results()
            .stream()
            .map(ChangeResult::change)
            .toList();

        // THEN

        JsonNode beforeValue = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_RECORD_VALUE);
        JsonNode afterValue = Jackson.JSON_OBJECT_MAPPER.readTree(BEFORE_AFTER_VALUE);
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaTableRecord.class)
            .withMetadata(actual.getFirst().getMetadata())  // IT'S OK - Metadata contains some dynamic data.
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.UPDATE)
                .withChange(StateChange.update("record",
                    V1KafkaTableRecordSpec
                        .builder()
                        .withTopic(TEST_TOPIC_COMPACTED)
                        .withHeader(KAFKA_RECORD_HEADER)
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                        .withValue(new DataValue(DataType.JSON, new DataHandle(beforeValue)))
                        .build(),
                    V1KafkaTableRecordSpec
                        .builder()
                        .withTopic(TEST_TOPIC_COMPACTED)
                        .withHeader(KAFKA_RECORD_HEADER)
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                        .withValue(new DataValue(DataType.JSON, new DataHandle(afterValue)))
                        .build()
                ))
                .build()
            )
            .build();
        Assertions.assertEquals(List.of(expected), actual);
    }
}