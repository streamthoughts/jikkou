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

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Example;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionDescriptorBuilderTest {

    static final List<Example> NO_EXAMPLES = Collections.emptyList();
    static final List<ConfigPropertySpec> NO_PROPERTIES = Collections.emptyList();

    @Test
    void shouldCreateBuilderFromDescriptor() {
        // GIVEN
        var descriptor = new DefaultExtensionDescriptor<>(
                "Name",
                "Title",
                "Description",
                NO_EXAMPLES,
                ExtensionCategory.EXTENSION,
                NO_PROPERTIES,
                "Provider",
                ExtensionDescriptorBuilderTest.class,
                ExtensionDescriptorBuilderTest.class.getClassLoader(),
                () -> null,
                true
        );
        // WHEN
        ExtensionDescriptor<ExtensionDescriptorBuilderTest> result = ExtensionDescriptorBuilder
                .create(descriptor)
                .build();
        // THEN
        Assertions.assertNotSame(result, descriptor);
        Assertions.assertEquals(result, descriptor);
    }
}