/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TopicConfigPredicateTest {


    @Test
    void shouldReturnTrueForDynamicTopicConfigEntryEnable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .dynamicTopicConfig(true);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseForDynamicTopicConfigEntryDisable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .dynamicTopicConfig(false);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnFalseForDefaultConfigEntryEnable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .defaultConfig(true);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DEFAULT_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseForDefaultConfigEntryDisable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .defaultConfig(false);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DEFAULT_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnTrueForDynamicBrokerConfigEntry() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .dynamicBrokerConfig(true);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseForDynamicBrokerConfigEntryDisable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .dynamicBrokerConfig(false);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnTrueForStaticBrokerConfigEntry() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .staticBrokerConfig(true);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseForStaticBrokerConfigEntryDisable() {
        // Given
        KafkaConfigPredicate predicate = new KafkaConfigPredicate()
                .staticBrokerConfig(false);
        // When
        boolean result = predicate.test(new ConfigEntry(
                "",
                "",
                ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG,
                false,
                false,
                null,
                ConfigEntry.ConfigType.STRING,
                null
        ));

        // Then
        Assertions.assertFalse(result);
    }
}