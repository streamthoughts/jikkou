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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultExtensionDescriptorFactoryTest {

    /**
     * @see io.streamthoughts.jikkou.common.annotation.InterfaceStability
     */
    public static final ExtensionAttribute EVOLVING_EXTENSION = new ExtensionAttribute("evolving");

    @Test
    void shouldCreateDescriptorForExtensionWithAnnotations() {
        // Given
        DefaultExtensionDescriptorFactory factory = new DefaultExtensionDescriptorFactory();

        // When
        ExtensionDescriptor<TestAnnotatedExtension> descriptor = factory.make(TestAnnotatedExtension.class, TestAnnotatedExtension::new);

        // Then
        Assertions.assertNotNull(descriptor);
        Assertions.assertNotNull(descriptor.supplier());
        Assertions.assertNotNull(descriptor.classLoader());
        Assertions.assertEquals("TestExtension", descriptor.name());
        Assertions.assertEquals("This is a test extension", descriptor.description());

        ExtensionMetadata expectedMetadata = new ExtensionMetadata();
        expectedMetadata.addAttribute(new ExtensionAttribute("named").add(
                "value", "TestExtension", ""
        ));
        expectedMetadata.addAttribute(new ExtensionAttribute("description").add(
                "value", "This is a test extension", ""
        ));
        expectedMetadata.addAttribute(new ExtensionAttribute("category").add(
                "value", "test", "<unknown>"
        ));
        expectedMetadata.addAttribute(EVOLVING_EXTENSION);

        Assertions.assertEquals(expectedMetadata, descriptor.metadata());
    }

    @Test
    void shouldCreateDescriptorForExtensionWithNoAnnotation() {
        // Given
        DefaultExtensionDescriptorFactory factory = new DefaultExtensionDescriptorFactory();

        // When
        ExtensionDescriptor<TestExtension> descriptor = factory.make(TestExtension.class, TestExtension::new);

        // Then
        Assertions.assertNotNull(descriptor);
        Assertions.assertNotNull(descriptor.supplier());
        Assertions.assertNotNull(descriptor.classLoader());
        Assertions.assertEquals("TestExtension", descriptor.name());
        Assertions.assertEquals("", descriptor.description());

        ExtensionMetadata expectedMetadata = new ExtensionMetadata();
        expectedMetadata.addAttribute(EVOLVING_EXTENSION);

        Assertions.assertEquals(expectedMetadata, descriptor.metadata());
    }

    public static class TestExtension implements Extension {}

    @Named("TestExtension")
    @Description( "This is a test extension")
    @Category("test")
    public static class TestAnnotatedExtension implements Extension {}

}