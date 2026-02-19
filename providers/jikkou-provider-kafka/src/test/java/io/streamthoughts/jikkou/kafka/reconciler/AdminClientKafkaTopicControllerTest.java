/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdminClientKafkaTopicControllerTest {

    @Test
    void shouldEnrichActualTopicWithLabelsFromExpected() {
        // GIVEN
        V1KafkaTopic actual = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .build())
                .build();

        V1KafkaTopic expected = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaTopic> actualList = new ArrayList<>(List.of(actual));
        AdminClientKafkaTopicController.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertEquals("my-service", actual.getMetadata().getLabels().get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenNoMatchingExpected() {
        // GIVEN
        V1KafkaTopic actual = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("other-topic")
                        .build())
                .build();

        V1KafkaTopic expected = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaTopic> actualList = new ArrayList<>(List.of(actual));
        AdminClientKafkaTopicController.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertTrue(actual.getMetadata().getLabels().isEmpty());
    }

    @Test
    void shouldPreserveSystemLabelsOnActualAfterEnrichment() {
        // GIVEN
        V1KafkaTopic actual = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .withLabel("jikkou.io/kafka.topic.id", "abc-123")
                        .build())
                .build();

        V1KafkaTopic expected = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaTopic> actualList = new ArrayList<>(List.of(actual));
        AdminClientKafkaTopicController.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        Map<String, Object> labels = actual.getMetadata().getLabels();
        assertEquals("abc-123", labels.get("jikkou.io/kafka.topic.id"));
        assertEquals("my-service", labels.get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenExpectedListIsEmpty() {
        // GIVEN
        V1KafkaTopic actual = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-topic")
                        .withLabel("jikkou.io/kafka.topic.id", "abc-123")
                        .build())
                .build();

        // WHEN
        List<V1KafkaTopic> actualList = new ArrayList<>(List.of(actual));
        AdminClientKafkaTopicController.enrichLabelsFromExpected(actualList, List.of());

        // THEN
        assertEquals(Map.of("jikkou.io/kafka.topic.id", "abc-123"),
                actual.getMetadata().getLabels());
    }
}
