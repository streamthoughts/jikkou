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
package io.streamthoughts.jikkou.kafka.validation;

import static io.streamthoughts.jikkou.kafka.validation.TopicMaxNumPartitionsValidation.VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG;

import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicMaxNumPartitionsValidationTest {

    TopicMaxNumPartitionsValidation validation;

    @BeforeEach
    void before() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.appConfiguration()).thenReturn(VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG.asConfiguration(1));

        validation = new TopicMaxNumPartitionsValidation();
        validation.init(context);
    }

    @Test
    void shouldThrowExceptionForInvalidMaxNumPartition() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(2)
                        .withReplicas((short)1)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);
        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForTopicWithNoNumPartition() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas((short)1)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForValidMaxNumPartition() {
        // Given
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(1)
                        .withReplicas((short)1)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);
        // Then
        Assertions.assertTrue(result.isValid());
    }
}