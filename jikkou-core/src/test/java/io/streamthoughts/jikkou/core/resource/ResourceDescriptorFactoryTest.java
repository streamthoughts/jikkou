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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceDescriptorFactoryTest {

    private ResourceDescriptorFactory factory;

    @BeforeEach
    void beforeEach() {
        this.factory = new ResourceDescriptorFactory();
    }

    @Test
    void shouldGetKindGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertEquals("Test", descriptor.kind());
    }

    @Test
    void shouldGetApiVersionGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertEquals("v1beta2", descriptor.apiVersion());
    }

    @Test
    void shouldGetGroupGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertEquals("test.jikkou.io", descriptor.group());
    }

    @Test
    void shouldGetDescriptionGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor =factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertEquals("Test description", descriptor.description());
    }

    @Test
    void shouldGetSingularNameGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertEquals("test", descriptor.singularName());
    }

    @Test
    void shouldGetPluralNameGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        Assertions.assertTrue(descriptor.pluralName().isPresent());
        Assertions.assertEquals("tests", descriptor.pluralName().get());
    }

    @Test
    void shouldGetShortNameGivenResourceClass() {
        // Given
        Class<ResourceDescriptorTest.TestResource> resourceClass = ResourceDescriptorTest.TestResource.class;
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(resourceClass), resourceClass);
        // Then
        HashSet<String> shortNames = new HashSet<>();
        shortNames.add("t");
        shortNames.add("ts");
        Assertions.assertEquals(shortNames, descriptor.shortNames());
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("Test")
    @Description("Test description")
    @Names(
            plural = "tests",
            singular = "test",
            shortNames = { "t" , "ts" }
    )
    static abstract class TestResource implements HasMetadata {};
}