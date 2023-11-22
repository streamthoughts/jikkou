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