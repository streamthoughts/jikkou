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