/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
            null,
            (unused) -> null,
            ExtensionDescriptorBuilderTest.class,
            ExtensionDescriptorBuilderTest.class.getClassLoader(),
            () -> null,
            null,
            true,
            null
        );
        // WHEN
        ExtensionDescriptor<ExtensionDescriptorBuilderTest> result = ExtensionDescriptorBuilder
            .builder(descriptor)
            .build();
        // THEN
        Assertions.assertNotSame(result, descriptor);
        Assertions.assertEquals(result, descriptor);
    }
}