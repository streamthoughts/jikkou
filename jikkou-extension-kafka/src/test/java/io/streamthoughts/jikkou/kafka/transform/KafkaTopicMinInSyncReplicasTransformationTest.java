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

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.Optional;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicMinInSyncReplicasTransformationTest {

    static Configuration CONFIGURATION = KafkaTopicMinInSyncReplicasTransformation.MIN_INSYNC_REPLICAS_CONFIG
            .asConfiguration(2);

    @Test
    void shouldEnforceConstraintForInvalidValue() {
        // Given
        KafkaTopicMinInSyncReplicasTransformation transformation = new KafkaTopicMinInSyncReplicasTransformation();
        transformation.configure(CONFIGURATION);

        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, 1))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, DefaultResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                2,
                transformed.getSpec().getConfigs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG).value()
        );
    }

    @Test
    void shouldEnforceConstraintForMissingValue() {
        // Given
        KafkaTopicMinInSyncReplicasTransformation transformation = new KafkaTopicMinInSyncReplicasTransformation();
        transformation.configure(CONFIGURATION);

        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, DefaultResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                2,
                transformed.getSpec().getConfigs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG).value()
        );
    }

    @Test
    void shouldNotEnforceConstraintForValidResource() {
        // Given
        KafkaTopicMinInSyncReplicasTransformation transformation = new KafkaTopicMinInSyncReplicasTransformation();
        transformation.configure(CONFIGURATION);

        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, 3))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, DefaultResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
         Assertions.assertEquals(
                3,
                transformed.getSpec().getConfigs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG).value()
        );
    }
}