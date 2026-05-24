/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TopicPartitionSelectorTest {

    @Test
    void shouldParseBareTopicAsAllPartitions() {
        TopicPartitionSelector selector = TopicPartitionSelector.parse("my-topic");

        assertEquals("my-topic", selector.topic());
        assertEquals(OptionalInt.empty(), selector.partition());
        assertTrue(selector.isAllPartitions());
    }

    @Test
    void shouldParseTopicWithPartitionZero() {
        TopicPartitionSelector selector = TopicPartitionSelector.parse("my-topic:0");

        assertEquals("my-topic", selector.topic());
        assertEquals(OptionalInt.of(0), selector.partition());
        assertFalse(selector.isAllPartitions());
    }

    @Test
    void shouldParseTopicWithPositivePartition() {
        TopicPartitionSelector selector = TopicPartitionSelector.parse("my-topic:42");

        assertEquals("my-topic", selector.topic());
        assertEquals(OptionalInt.of(42), selector.partition());
        assertFalse(selector.isAllPartitions());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",            // empty
        ":3",          // empty topic
        "my-topic:",   // empty partition
        "my-topic:abc",// non-integer partition
        "my-topic:-1", // negative partition
        "my-topic:1:2" // multiple colons
    })
    void shouldThrowForMalformedInput(String raw) {
        assertThrows(IllegalArgumentException.class, () -> TopicPartitionSelector.parse(raw));
    }

    @Test
    void shouldThrowForNullInput() {
        assertThrows(IllegalArgumentException.class, () -> TopicPartitionSelector.parse(null));
    }
}
