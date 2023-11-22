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
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
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
                DefaultResourceListObject.empty(),
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
                DefaultResourceListObject.empty(),
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