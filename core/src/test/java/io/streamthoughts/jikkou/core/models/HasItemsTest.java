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
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        List<? extends HasMetadata> result = list.getAllByKind(TestResource.class);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetAllResourcesByKindString() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        List<? extends HasMetadata> result = list.getAllByKind("Test");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetNoResourceByKindString() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        List<? extends HasMetadata> result = list.getAllByKind("Foo");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetAllResourcesByApiVersionString() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        List<? extends HasMetadata> result = list.getAllByApiVersion("core/v1");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldGetResourceByName() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        TestResource resource = list.getByName("test", TestResource.class);

        // Then
        Assertions.assertNotNull(resource);
        Assertions.assertEquals("test", resource.getMetadata().getName());
    }

    @Test
    void shouldThrowExceptionForNoResourceMatchingResourceName() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When / Then
        Assertions.assertThrowsExactly(JikkouRuntimeException.class, () -> list.getByName("???", TestResource.class));
    }

    @Test
    void shouldReturnNonEmptyOptionalForMatchingResourceName() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        Optional<? extends HasMetadata> result = list.findByName("test");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalForNonMatchingResourceName() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test")
        ));
        // When
        Optional<? extends HasMetadata> result = list.findByName("???");

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionGivenDuplicateResourceName() {
        // Given
        DefaultResourceListObject<HasMetadata> list = new DefaultResourceListObject<>(List.of(
                getTestResourceForName("test"),
                getTestResourceForName("test")
        ));
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