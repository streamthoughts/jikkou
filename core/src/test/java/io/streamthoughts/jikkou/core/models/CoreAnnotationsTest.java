/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoreAnnotationsTest {

    @Test
    void shouldReturnProviderAnnotationWhenPresent() {
        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test-resource")
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod")
                        .build(),
                null
        );

        Optional<String> result = CoreAnnotations.findProviderAnnotation(resource);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("kafka-prod", result.get());
    }

    @Test
    void shouldReturnEmptyWhenProviderAnnotationAbsent() {
        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test-resource")
                        .build(),
                null
        );

        Optional<String> result = CoreAnnotations.findProviderAnnotation(resource);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenMetadataIsNull() {
        GenericResource resource = new GenericResource("core/v1", "Test", null, null);

        Optional<String> result = CoreAnnotations.findProviderAnnotation(resource);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnProviderAnnotationValueAsString() {
        // Test that non-string values are converted to string
        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test")
                        .withAnnotations(Map.of(CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-staging"))
                        .build(),
                null
        );

        Optional<String> result = CoreAnnotations.findProviderAnnotation(resource);
        Assertions.assertEquals("kafka-staging", result.orElse(null));
    }
}
