/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SupportedResourceQualifierTest {

    @Test
    void shouldFilterDescriptorsForMatchingResourceType() {
        SupportedResourceQualifier<TestExtension> qualifier = new SupportedResourceQualifier<>(
                ResourceType.of("Foo", "test.jikkou.io/v1"));

        ExtensionDescriptor<TestExtension> descriptor = getDescriptor(TestExtension.class);
        Stream<ExtensionDescriptor<TestExtension>> filtered = qualifier.filter(TestExtension.class, Stream.of(descriptor));
        Assertions.assertEquals(List.of(getDescriptor(TestExtension.class)), filtered.toList());
    }


    @Test
    void shouldFilterDescriptorsForNotMatchingResourceType() {
        SupportedResourceQualifier<TestExtension> qualifier = new SupportedResourceQualifier<>(
                ResourceType.of("Dummy", "test.jikkou.io/v1"));

        ExtensionDescriptor<TestExtension> descriptor = getDescriptor(TestExtension.class);
        Stream<ExtensionDescriptor<TestExtension>> filtered = qualifier.filter(TestExtension.class, Stream.of(descriptor));
        Assertions.assertTrue(filtered.toList().isEmpty());
    }

    private static <T extends Extension> ExtensionDescriptor<T> getDescriptor(Class<T> clazz) {
        ExtensionDescriptorFactory factory = new DefaultExtensionDescriptorFactory();
        return factory.make(clazz, () -> null);
    }

    @SupportedResource(apiVersion = "test.jikkou.io/v1", kind = "Bar")
    @SupportedResource(apiVersion = "test.jikkou.io/v1", kind = "Foo")
    public static class TestExtension implements Extension { }
}