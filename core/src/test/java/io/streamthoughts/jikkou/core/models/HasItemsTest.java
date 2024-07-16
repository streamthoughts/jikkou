/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HasItemsTest {

    @Test
    void shouldGetAllResourcesByKindClass() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        List<? extends HasMetadata> result = list.getAllByKind(TestResource.class);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetAllResourcesByKindString() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        List<? extends HasMetadata> result = list.getAllByKind("Test");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetNoResourceByKindString() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        List<? extends HasMetadata> result = list.getAllByKind("Foo");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetAllResourcesByApiVersionString() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        List<? extends HasMetadata> result = list.getAllByApiVersion("core/v1");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetResourceByName() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        TestResource resource = list.getByName("test", TestResource.class);

        // Then
        Assertions.assertNotNull(resource);
        Assertions.assertEquals("test", resource.getMetadata().getName());
    }

    @Test
    void shouldThrowExceptionForNoResourceMatchingResourceName() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When / Then
        Assertions.assertThrowsExactly(JikkouRuntimeException.class, () -> list.getByName("???", TestResource.class));
    }

    @Test
    void shouldReturnNonEmptyOptionalForMatchingResourceName() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        Optional<? extends HasMetadata> result = list.findByName("test");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalForNonMatchingResourceName() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(getTestResourceForName("test"));
        // When
        Optional<? extends HasMetadata> result = list.findByName("???");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionGivenDuplicateResourceName() {
        // Given
        ResourceList<HasMetadata> list = ResourceList.of(
                getTestResourceForName("test"),
                getTestResourceForName("test")
        );
        // When - Then
        Assertions.assertThrowsExactly(JikkouRuntimeException.class, () -> list.getByName("???", TestResource.class));
    }

    private static TestResource getTestResourceForName(String name) {
        return new TestResource()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(name)
                        .build()
                );
    }
}