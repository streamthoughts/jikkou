/*
 * Copyright 2021 The original authors
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