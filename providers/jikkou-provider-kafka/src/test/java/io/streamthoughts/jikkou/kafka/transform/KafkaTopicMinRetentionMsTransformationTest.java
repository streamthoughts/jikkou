/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.transform;

import static io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinRetentionMsTransformation.MIN_RETENTIONS_MS_CONFIG;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Optional;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KafkaTopicMinRetentionMsTransformationTest {

    public static final long MIN_VALUE = 1000L;

    KafkaTopicMinRetentionMsTransformation transformation;

    @BeforeEach
    void beforeEach() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(MIN_RETENTIONS_MS_CONFIG.asConfiguration(MIN_VALUE));
        transformation = new KafkaTopicMinRetentionMsTransformation();
        transformation.init(context);
    }


    @Test
    void shouldEnforceConstraintForInvalidValue() {
        // Given
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.RETENTION_MS_CONFIG, -1L))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, ResourceList.empty(), ReconciliationContext.Default.EMPTY);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                MIN_VALUE,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }

    @Test
    void shouldEnforceConstraintForMissingValue() {
        // Given
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, ResourceList.empty(), ReconciliationContext.Default.EMPTY);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                MIN_VALUE,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }

    @Test
    void shouldNotEnforceConstraintForValidValue() {
        // Given
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withConfigs(Configs.of(TopicConfig.RETENTION_MS_CONFIG, 5000L))
                        .build())
                .build();
        // When
        Optional<V1KafkaTopic> result = transformation
                .transform(resource, ResourceList.empty(), ReconciliationContext.Default.EMPTY);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());

        V1KafkaTopic transformed = result.get();
        Assertions.assertEquals(
                5000L,
                transformed.getSpec().getConfigs().get(TopicConfig.RETENTION_MS_CONFIG).value()
        );
    }
}