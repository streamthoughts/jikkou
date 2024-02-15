/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import static io.streamthoughts.jikkou.core.models.HasMetadataAcceptable.getSupportedResources;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HasMetadataAcceptableTest {

    @Test
    void shouldGetAcceptedResources() {
        // When
        List<ResourceType> types = getSupportedResources(TestHasMetadataAcceptable.class);

        // Then
        Assertions.assertNotNull(types);
        Assertions.assertEquals(1, types.size());
        Assertions.assertEquals(ResourceType.of(TestResource.class), types.get(0));
    }

    @Test
    void shouldReturnTrueForClassAcceptingResource() {
        // Given
        TestHasMetadataAcceptable acceptable = new TestHasMetadataAcceptable();
        // When - Then
        Assertions.assertTrue(acceptable.canAccept(ResourceType.of(TestResource.class)));
    }

    @Test
    void shouldAcceptResourceForKindOnly() {
        // Given
        TestHasMetadataAcceptableWithKindOnly acceptable = new TestHasMetadataAcceptableWithKindOnly();

        // When / Then
        Assertions.assertTrue(acceptable.canAccept(ResourceType.of(TestResource.class)));
    }

    @SupportedResource(type = TestResource.class)
    public static class TestHasMetadataAcceptable implements HasMetadataAcceptable { }

    @SupportedResource(kind = "Test")
    public static class TestHasMetadataAcceptableWithKindOnly implements HasMetadataAcceptable { }

}