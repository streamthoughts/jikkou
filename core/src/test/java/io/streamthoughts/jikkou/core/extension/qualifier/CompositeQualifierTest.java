/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompositeQualifierTest {

    @Test
    void shouldFilterDescriptorsForCompositeQualifier() {
        Qualifier<CompositeQualifierTest> qualifier = Qualifiers.byQualifiers(
            new EnabledQualifier<>(),
            new NamedQualifier<>("Test")
        );

        ExtensionDescriptor<CompositeQualifierTest> descriptor = getDescriptor(CompositeQualifierTest.class, true);
        List<ExtensionDescriptor<CompositeQualifierTest>> result = qualifier
            .filter(CompositeQualifierTest.class, Stream.of(descriptor)).toList();
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
            null,
            (unused) -> null,
            clazz,
            clazz.getClassLoader(),
            () -> null,
            null,
            isEnabled,
            null
        );
    }
}