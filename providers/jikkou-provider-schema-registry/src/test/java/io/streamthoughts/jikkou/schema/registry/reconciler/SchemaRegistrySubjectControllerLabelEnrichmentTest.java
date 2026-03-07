/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchemaRegistrySubjectControllerLabelEnrichmentTest {

    @Test
    void shouldEnrichActualSubjectWithLabelsFromExpected() {
        // GIVEN
        V1SchemaRegistrySubject actual = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .build())
                .build();

        V1SchemaRegistrySubject expected = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1SchemaRegistrySubject> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertEquals("my-service", actual.getMetadata().getLabels().get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenNoMatchingExpected() {
        // GIVEN
        V1SchemaRegistrySubject actual = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("other-subject")
                        .build())
                .build();

        V1SchemaRegistrySubject expected = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1SchemaRegistrySubject> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        assertTrue(actual.getMetadata().getLabels().isEmpty());
    }

    @Test
    void shouldPreserveSystemLabelsOnActualAfterEnrichment() {
        // GIVEN
        V1SchemaRegistrySubject actual = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .withLabel("jikkou.io/schema-registry.subject", "my-subject")
                        .build())
                .build();

        V1SchemaRegistrySubject expected = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .withLabel("my-label", "my-service")
                        .build())
                .build();

        // WHEN
        List<V1SchemaRegistrySubject> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of(expected));

        // THEN
        Map<String, Object> labels = actual.getMetadata().getLabels();
        assertEquals("my-subject", labels.get("jikkou.io/schema-registry.subject"));
        assertEquals("my-service", labels.get("my-label"));
    }

    @Test
    void shouldLeaveActualUnchangedWhenExpectedListIsEmpty() {
        // GIVEN
        V1SchemaRegistrySubject actual = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName("my-subject")
                        .withLabel("jikkou.io/schema-registry.subject", "my-subject")
                        .build())
                .build();

        // WHEN
        List<V1SchemaRegistrySubject> actualList = new ArrayList<>(List.of(actual));
        Controller.enrichLabelsFromExpected(actualList, List.of());

        // THEN
        assertEquals(Map.of("jikkou.io/schema-registry.subject", "my-subject"),
                actual.getMetadata().getLabels());
    }
}
