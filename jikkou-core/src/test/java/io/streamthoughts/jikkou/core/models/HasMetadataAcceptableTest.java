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

import static io.streamthoughts.jikkou.core.models.HasMetadataAcceptable.getAcceptedResources;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.resource.converter.ResourceConverter;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HasMetadataAcceptableTest {

    @Test
    void shouldGetAcceptedResources() {
        // When
        List<ResourceType> types = getAcceptedResources(TestHasMetadataAcceptable.class);

        // Then
        Assertions.assertNotNull(types);
        Assertions.assertEquals(1, types.size());
        Assertions.assertEquals(ResourceType.create(TestResource.class), types.get(0));
    }

    @Test
    void shouldReturnTrueForClassAcceptingResource() {
        // Given
        TestHasMetadataAcceptable acceptable = new TestHasMetadataAcceptable();
        // When - Then
        Assertions.assertTrue(acceptable.canAccept(ResourceType.create(TestResource.class)));
    }

    @Test
    void shouldReturnResourceConverter() {
        // Given
        TestHasMetadataAcceptable acceptable = new TestHasMetadataAcceptable();

        // When
        ResourceConverter<HasMetadata, HasMetadata> converter = acceptable
                .getResourceConverter(new TestResource());

        Assertions.assertNotNull(converter);
        Assertions.assertEquals(TestResourceConverter.class, converter.getClass());
    }

    @Test
    void shouldAcceptResourceForKindOnly() {
        // Given
        TestHasMetadataAcceptableWithKindOnly acceptable = new TestHasMetadataAcceptableWithKindOnly();

        // When / Then
        Assertions.assertTrue(acceptable.canAccept(ResourceType.create(TestResource.class)));
    }

    @AcceptsResource(type = TestResource.class, converter = TestResourceConverter.class)
    public static class TestHasMetadataAcceptable implements HasMetadataAcceptable { }

    @AcceptsResource(kind = "Test")
    public static class TestHasMetadataAcceptableWithKindOnly implements HasMetadataAcceptable { }

    public static class TestResourceConverter implements ResourceConverter<TestResource, TestResource> {

        @Override
        public @NotNull List<TestResource> convertFrom(@NotNull List<TestResource> resources) {
            return null;
        }

        @Override
        public @NotNull List<TestResource> convertTo(@NotNull List<TestResource> resources) {
            return null;
        }
    }
}