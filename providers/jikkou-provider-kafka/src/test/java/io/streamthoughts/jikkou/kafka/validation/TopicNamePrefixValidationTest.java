/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import static io.streamthoughts.jikkou.kafka.validation.TopicNamePrefixValidation.VALIDATION_TOPIC_NAME_PREFIXES_CONFIG;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicNamePrefixValidationTest {

    @Test
    void shouldThrowExceptionForMissingConfig() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(Configuration.empty());

        Assertions.assertThrows(ConfigException.class, () -> new TopicNamePrefixValidation().init(context));
    }

    @Test
    void shouldThrowExceptionForTopicNotStartingWithPrefix() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.asConfiguration(List.of("test-")));

        var validation = new TopicNamePrefixValidation();
        validation.init(context);

        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("dummy")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);

        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotThrowForTopicStartingWithPrefix() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.asConfiguration(List.of("test-")));

        var validation = new TopicNamePrefixValidation();
        validation.init(context);

        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-dummy")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                        .build()
                )
                .build();
        // When
        ValidationResult result = validation.validate(topic);

        // Then
        Assertions.assertTrue(result.isValid());
    }
}