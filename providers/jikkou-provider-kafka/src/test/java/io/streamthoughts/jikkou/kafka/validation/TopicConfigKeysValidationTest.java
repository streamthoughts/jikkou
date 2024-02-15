/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopicConfigKeysValidationTest {

    TopicConfigKeysValidation validation;

    @BeforeEach
    public void before() {
        validation = new TopicConfigKeysValidation();
    }

    @Test
    void shouldReturnErrorsForTopicWithInvalidConfigKey() {
        // Given
        var resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                        .withConfigs(Configs.of("bad.key1", "???", "bad.key2", "???"))
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(resource);

        // Then
        Assertions.assertEquals(2, result.errors().size());
    }

    @Test
    void shouldNotReturnErrorForTopicWithValidConfigKey() {
        // Given
        var resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                        .withConfigs(Configs.of("retention.ms", "???"))
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(resource);
        // Then
        Assertions.assertTrue(result.isValid());
    }
}