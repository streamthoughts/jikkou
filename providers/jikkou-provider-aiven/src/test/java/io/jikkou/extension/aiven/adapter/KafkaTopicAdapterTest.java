/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.adapter;

import io.jikkou.core.io.Jackson;
import io.jikkou.extension.aiven.api.data.KafkaTopicInfo;
import io.jikkou.extension.aiven.api.data.PartitionInfo;
import io.jikkou.kafka.models.V1KafkaTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicAdapterTest {

    private static final long THREE_GIB = 3221225472L;
    private static final long FOUR_GIB = 4294967296L;

    @Test
    void shouldDeserializePartitionSizeGreaterThanTwoGigabytes() throws Exception {
        // Given: Aiven reports the partition size in bytes, which exceeds the 32-bit range past 2GiB.
        String json = """
            {
              "earliest_offset": 0,
              "latest_offset": 42,
              "partition": 0,
              "size": 3221225472
            }
            """;

        // When
        PartitionInfo result = Jackson.json().readValue(json, PartitionInfo.class);

        // Then
        Assertions.assertEquals(THREE_GIB, result.size());
    }

    @Test
    void shouldDeserializePartitionOffsetsGreaterThanIntegerMaxValue() throws Exception {
        // Given: Kafka offsets are 64-bit and exceed Integer.MAX_VALUE on high-throughput topics.
        String json = """
            {
              "earliest_offset": 2147483648,
              "latest_offset": 4294967296,
              "partition": 0,
              "size": 1024
            }
            """;

        // When
        PartitionInfo result = Jackson.json().readValue(json, PartitionInfo.class);

        // Then
        Assertions.assertEquals(2147483648L, result.earliestOffset());
        Assertions.assertEquals(FOUR_GIB, result.latestOffset());
    }

    @Test
    void shouldMapToV1KafkaTopicGivenPartitionsGreaterThanTwoGigabytes() throws Exception {
        // Given
        String json = """
            {
              "topic_name": "my-topic",
              "partitions": [
                {"earliest_offset": 0, "latest_offset": 10000000000, "partition": 0, "size": 3221225472},
                {"earliest_offset": 0, "latest_offset": 10000000001, "partition": 1, "size": 4294967296}
              ],
              "replication": 3,
              "config": {},
              "state": "ACTIVE",
              "tags": []
            }
            """;
        KafkaTopicInfo topicInfo = Jackson.json().readValue(json, KafkaTopicInfo.class);
        Assertions.assertEquals(THREE_GIB, topicInfo.partitions().get(0).size());
        Assertions.assertEquals(FOUR_GIB, topicInfo.partitions().get(1).size());

        // When
        V1KafkaTopic result = KafkaTopicAdapter.map(topicInfo, configInfo -> true);

        // Then
        Assertions.assertEquals("my-topic", result.getMetadata().getName());
        Assertions.assertEquals(2, result.getSpec().getPartitions());
        Assertions.assertEquals((short) 3, result.getSpec().getReplicas());
    }
}
