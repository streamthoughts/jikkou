/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import static io.streamthoughts.jikkou.kafka.validation.TopicMinReplicationFactorValidation.VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG;

import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicMinReplicationFactorValidationTest {

    TopicMinReplicationFactorValidation validation;

    @BeforeEach
    void before() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG.asConfiguration(1));
        validation = new TopicMinReplicationFactorValidation();
        validation.init(context);
    }

    @Test
    void shouldThrowExceptionForInvalidMinReplicationFactor() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(1)
                        .withReplicas((short) 0)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);

        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotThrowExceptionForTopicWithNoReplicationFactor() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(1)
                        .withReplicas((short) -1)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldNotThrowExceptionForTopicWithValidReplicationFactor() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);

        // Then
        Assertions.assertTrue(result.isValid());
    }

}