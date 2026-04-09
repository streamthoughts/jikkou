/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KafkaConnectorControllerTest {

    @Test
    void shouldEnrichActualConnectorWithLabelsFromExpected() {
        // GIVEN
        V1KafkaConnector actual = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .build())
                .build();

        V1KafkaConnector expected = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaConnector> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertEquals("my-service", actual.getMetadata().getLabels().get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenNoMatchingExpected() {
        // GIVEN
        V1KafkaConnector actual = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("other-connector")
                        .build())
                .build();

        V1KafkaConnector expected = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaConnector> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertTrue(actual.getMetadata().getLabels().isEmpty());
    }

    @Test
    void shouldPreserveSystemLabelsOnActualAfterEnrichment() {
        // GIVEN
        V1KafkaConnector actual = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .withLabel("jikkou.io/kafka-connect.cluster", "cluster-1")
                        .build())
                .build();

        V1KafkaConnector expected = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1KafkaConnector> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        Map<String, Object> labels = actual.getMetadata().getLabels();
        assertEquals("cluster-1", labels.get("jikkou.io/kafka-connect.cluster"));
        assertEquals("my-service", labels.get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenExpectedListIsEmpty() {
        // GIVEN
        V1KafkaConnector actual = V1KafkaConnector.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-connector")
                        .withLabel("jikkou.io/kafka-connect.cluster", "cluster-1")
                        .build())
                .build();

        // WHEN
        List<V1KafkaConnector> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of());

        // THEN
        assertEquals(Map.of("jikkou.io/kafka-connect.cluster", "cluster-1"),
                actual.getMetadata().getLabels());
    }
}
