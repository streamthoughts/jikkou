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
package io.streamthoughts.jikkou.core.extension.builder;

import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionDescriptorBuilderTest {

    @Test
    void shouldCreateBuilderFromDescriptor() {
        var descriptor = new DefaultExtensionDescriptor<>(
                "Name",
                "Description",
                "test",
                ExtensionDescriptorBuilderTest.class,
                ExtensionDescriptorBuilderTest.class.getClassLoader(),
                () -> null,
                true
        );

        ExtensionDescriptor<ExtensionDescriptorBuilderTest> result = ExtensionDescriptorBuilder
                .create(descriptor)
                .build();
        Assertions.assertFalse(result == descriptor);
        Assertions.assertEquals(result, descriptor);
    }
}