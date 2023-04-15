/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Description;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import io.streamthoughts.jikkou.api.model.annotations.Names;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceDescriptorTest {

    @Test
    void shouldGetKindGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertEquals("Test", descriptor.kind());
    }

    @Test
    void shouldGetApiVersionGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertEquals("v1beta2", descriptor.apiVersion());
    }

    @Test
    void shouldGetGroupGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertEquals("test.jikkou.io", descriptor.group());
    }

    @Test
    void shouldGetDescriptionGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertEquals("Test description", descriptor.description());
    }

    @Test
    void shouldGetSingularNameGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertEquals("test", descriptor.singularName());
    }

    @Test
    void shouldGetPluralNameGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
        // Then
        Assertions.assertTrue(descriptor.pluralName().isPresent());
        Assertions.assertEquals("tests", descriptor.pluralName().get());
    }

    @Test
    void shouldGetShortNameGivenResourceClass() {
        // Given
        Class<TestResource> resourceClass = TestResource.class;
        // When
        ResourceDescriptor descriptor = new ResourceDescriptor(resourceClass);
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
    static class TestResource implements HasMetadata {
        @Override
        public ObjectMeta getMetadata() { return null; }

        @Override
        public HasMetadata withMetadata(ObjectMeta objectMeta) { return null; }

        @Override
        public String getApiVersion() { return null; }

        @Override
        public String getKind() { return null; }
    }
}
