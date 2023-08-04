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
package io.streamthoughts.jikkou.kafka.transform;

import static io.streamthoughts.jikkou.kafka.transform.KafkaTopicMaxNumPartitionsTransformation.MAX_NUM_PARTITIONS_CONFIG;

import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicMaxNumPartitionsTransformationTest {

    @Test
    void shouldEnforceConstraintForInvalidValue() {
        // Given
        KafkaTopicMaxNumPartitionsTransformation transformation = new KafkaTopicMaxNumPartitionsTransformation();
        transformation.configure(MAX_NUM_PARTITIONS_CONFIG.asConfiguration(10));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(100)
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(10, transformed.getSpec().getPartitions());
    }

    @Test
    void shouldNotEnforceConstraintForValidValue() {
        // Given
        KafkaTopicMaxNumPartitionsTransformation transformation = new KafkaTopicMaxNumPartitionsTransformation();
        transformation.configure(MAX_NUM_PARTITIONS_CONFIG.asConfiguration(10));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(4)
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(4, transformed.getSpec().getPartitions());
    }
}