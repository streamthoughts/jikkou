/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import static io.streamthoughts.jikkou.kafka.validation.TopicNameRegexValidation.VALIDATION_TOPIC_NAME_REGEX_CONFIG;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
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

class TopicNameRegexValidationTest {

    TopicNameRegexValidation validation;

    @BeforeEach
    void before() {
        validation = new TopicNameRegexValidation();
    }

    @Test
    void shouldThrowExceptionForMissingConfig() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.appConfiguration()).thenReturn(Configuration.empty());
        Assertions.assertThrows(ConfigException.class, () -> validation.init(context));
    }

    @Test
    void shouldThrowExceptionForInvalidRegex() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.appConfiguration()).thenReturn(VALIDATION_TOPIC_NAME_REGEX_CONFIG.asConfiguration(""));

        // When
        Assertions.assertThrows(ConfigException.class, () -> new TopicNameRegexValidation().init(context));
    }

    @Test
    void shouldThrowExceptionForTopicNameNotMatching() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.appConfiguration()).thenReturn(VALIDATION_TOPIC_NAME_REGEX_CONFIG.asConfiguration("test-"));

        var validation =  new TopicNameRegexValidation();
        validation.init(context);

        new TopicNameRegexValidation();
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
}