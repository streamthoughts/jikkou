/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.action;

import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.action.ExecutionStatus;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.kafka.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.action.TruncateKafkaTopicRecords.TopicPartitionLowWatermark;
import io.streamthoughts.jikkou.kafka.action.TruncateKafkaTopicRecords.TruncatedKafkaTopicRecordsResult;
import io.streamthoughts.jikkou.kafka.action.TruncateKafkaTopicRecords.V1TruncatedKafkaTopicRecords;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TruncateKafkaTopicRecordsTest extends BaseExtensionProviderIT {

    public static final String TEST_TOPIC = "test";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC);

    @Test
    void shouldSucceedTruncateTopicGivenExistingTopic() {
        // Given
        Instant now = Instant.now();
        createTopic(TEST_TOPIC);
        try (var producer = new KafkaProducer<String, String>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()
        ))) {
            IntStream.range(0, 10).forEach(integer -> {
                producer.send(new ProducerRecord<>(
                    TEST_TOPIC,
                    null,
                    now.minusSeconds(10).toEpochMilli(),
                    "v" + integer, null, null));
            });
        }

        // When
        Configuration configuration = Configuration.of(
            TruncateKafkaTopicRecords.Config.TOPIC.key(), TEST_TOPIC,
            TruncateKafkaTopicRecords.Config.TO_DATETIME.key(), DATE_TIME_FORMATTER.format(Instant.now())
        );
        ApiActionResultSet<V1TruncatedKafkaTopicRecords> resultSet = api.execute(TruncateKafkaTopicRecords.NAME, configuration);

        // Then
        List<ExecutionResult<V1TruncatedKafkaTopicRecords>> items = resultSet.results();

        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, items.getFirst().status());

        V1TruncatedKafkaTopicRecords expected = new V1TruncatedKafkaTopicRecords(new TruncatedKafkaTopicRecordsResult(
            TEST_TOPIC,
            List.of(new TopicPartitionLowWatermark(0, 10)))
        );
        Assertions.assertEquals(expected, items.getFirst().data());
    }

    @Test
    void shouldFailedTruncateTopicGivenUnknownTopic() {
        // Given
        Instant now = Instant.now();
        // When
        Configuration configuration = Configuration.of(
            TruncateKafkaTopicRecords.Config.TOPIC.key(), TEST_TOPIC,
            TruncateKafkaTopicRecords.Config.TO_DATETIME.key(), DATE_TIME_FORMATTER.format(Instant.now())
        );
        ApiActionResultSet<V1TruncatedKafkaTopicRecords> resultSet = api.execute(TruncateKafkaTopicRecords.NAME, configuration);

        // Then
        List<ExecutionResult<V1TruncatedKafkaTopicRecords>> items = resultSet.results();
        Assertions.assertEquals(1, items.size());

        Assertions.assertEquals(ExecutionStatus.FAILED, items.getFirst().status());
        V1TruncatedKafkaTopicRecords expected = new V1TruncatedKafkaTopicRecords(new TruncatedKafkaTopicRecordsResult(
            TEST_TOPIC,
            null)
        );
        Assertions.assertEquals(expected, items.getFirst().data());
    }
}