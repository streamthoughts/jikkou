/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.Optional;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicMaxRetentionMsTransformationTest {

    public static final long MAX_VALUE = 1000;

    @Test
    void shouldEnforceConstraintForInvalidValue() {
        // Given
        KafkaTopicMaxRetentionMsTransformation transformation = new KafkaTopicMaxRetentionMsTransformation();
        transformation.configure(KafkaTopicMaxRetentionMsTransformation.MAX_RETENTIONS_MS_CONFIG.asConfiguration(MAX_VALUE));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.RETENTION_MS_CONFIG, 5000L))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                MAX_VALUE,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }

    @Test
    void shouldEnforceConstraintForMissingValue() {
        // Given
        KafkaTopicMaxRetentionMsTransformation transformation = new KafkaTopicMaxRetentionMsTransformation();
        transformation.configure(KafkaTopicMaxRetentionMsTransformation.MAX_RETENTIONS_MS_CONFIG.asConfiguration(MAX_VALUE));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                MAX_VALUE,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }

    @Test
    void shouldNotEnforceConstraintForValidValue() {
        // Given
        KafkaTopicMaxRetentionMsTransformation transformation = new KafkaTopicMaxRetentionMsTransformation();
        transformation.configure(KafkaTopicMaxRetentionMsTransformation.MAX_RETENTIONS_MS_CONFIG.asConfiguration(MAX_VALUE));
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.RETENTION_MS_CONFIG, -1L))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, GenericResourceListObject.of(Collections.emptyList()));

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                -1L,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }
}