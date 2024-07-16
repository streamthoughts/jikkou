/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnrichMetadataTransformationTest {


    @Test
    void shouldEnrichResourceWithLabels() {
        // Given
        EnrichMetadataTransformation transformation = new EnrichMetadataTransformation();

        GenericResource resource = new GenericResource(
                "apiVersion",
                "kind",
                new ObjectMeta(),
                null,
                null
        );
        // When
        HasMetadata transformed = transformation.transform(
                resource,
                ResourceList.empty(),
                ReconciliationContext.builder()
                        .label(new NamedValue("label", "value"))
                        .build()
        ).get();

        // Then
        Assertions.assertEquals(
                resource.withMetadata(ObjectMeta
                        .builder()
                                .withLabel("label", "value")
                        .build()),
                transformed);
    }

    @Test
    void shouldEnrichResourceWithAnnotations() {
        // Given
        EnrichMetadataTransformation transformation = new EnrichMetadataTransformation();

        GenericResource resource = new GenericResource(
                "apiVersion",
                "kind",
                new ObjectMeta(),
                null,
                null
        );
        // When
        HasMetadata transformed = transformation.transform(
                resource,
                ResourceList.empty(),
                ReconciliationContext.builder()
                        .annotation(new NamedValue("annotation", "value"))
                        .build()
        ).get();

        // Then
        Assertions.assertEquals(
                resource.withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("annotation", "value")
                        .build()),
                transformed);
    }

}