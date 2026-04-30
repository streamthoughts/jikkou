/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.resource;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.annotation.Names;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceType;
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

    @Test
    void shouldReadLocalNameFromNamesAnnotation() {
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(ResourceWithLocal.class), ResourceWithLocal.class);
        // Then
        Assertions.assertTrue(descriptor.localName().isPresent());
        Assertions.assertEquals("topics", descriptor.localName().get());
    }

    @Test
    void shouldTreatEmptyLocalNameAsAbsent() {
        // When
        ResourceDescriptor descriptor = factory.make(ResourceType.of(ResourceWithoutLocal.class), ResourceWithoutLocal.class);
        // Then
        Assertions.assertTrue(descriptor.localName().isEmpty());
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("Test")
    @Description("Test description")
    @Names(
            plural = "tests",
            singular = "test",
            shortNames = { "t" , "ts" }
    )
    static abstract class TestResource implements HasMetadata {}

    @ApiVersion("kafka.jikkou.io/v1beta2")
    @Kind("KafkaTopic")
    @Names(plural = "kafkatopics", singular = "kafkatopic", local = "topics")
    static abstract class ResourceWithLocal implements HasMetadata {}

    @ApiVersion("kafka.jikkou.io/v1beta2")
    @Kind("KafkaAcl")
    @Names(plural = "kafkaacls", singular = "kafkaacl")
    static abstract class ResourceWithoutLocal implements HasMetadata {}
}