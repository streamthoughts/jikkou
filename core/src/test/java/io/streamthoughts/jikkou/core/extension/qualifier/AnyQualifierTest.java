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

class AnyQualifierTest {

    @Test
    void shouldFilterDescriptorsForAnyQualifiers() {
        Qualifier<AnyQualifierTest> qualifier = Qualifiers.byAnyQualifiers(
            new EnabledQualifier<>(),
            new NamedQualifier<>("???")
        );

        ExtensionDescriptor<AnyQualifierTest> descriptor = getDescriptor(AnyQualifierTest.class, true);
        List<ExtensionDescriptor<AnyQualifierTest>> result = qualifier
            .filter(AnyQualifierTest.class, Stream.of(descriptor)).toList();
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