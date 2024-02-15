/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import static org.junit.jupiter.api.Assertions.*;

import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnabledQualifierTest {

    @Test
    void shouldFilterDescriptorsForEnabledTrue() {
        EnabledQualifier<EnabledQualifierTest> qualifier = new EnabledQualifier<>();

        ExtensionDescriptor<EnabledQualifierTest> descriptor = getDescriptor(EnabledQualifierTest.class, true);
        List<ExtensionDescriptor<EnabledQualifierTest>> result = qualifier
                .filter(EnabledQualifierTest.class, Stream.of(descriptor)).toList();
        Assertions.assertEquals(List.of(descriptor), result);
    }

    @Test
    void shouldFilterDescriptorsForEnabledFalse() {
        EnabledQualifier<EnabledQualifierTest> qualifier = new EnabledQualifier<>(false);

        ExtensionDescriptor<EnabledQualifierTest> descriptor = getDescriptor(EnabledQualifierTest.class, false);
        List<ExtensionDescriptor<EnabledQualifierTest>> result = qualifier
                .filter(EnabledQualifierTest.class, Stream.of(descriptor)).toList();
        Assertions.assertEquals(List.of(descriptor), result);
    }

    private static <T> ExtensionDescriptor<T> getDescriptor(Class<T> clazz, boolean isEnabled) {
        return new DefaultExtensionDescriptor<>(
                "Test",
                "Title",
                "Description",
                Collections.emptyList(),
                ExtensionCategory.EXTENSION,
                Collections.emptyList(),
                "Provider",
                clazz,
                clazz.getClassLoader(),
                () -> null,
                isEnabled
        );
    }
}