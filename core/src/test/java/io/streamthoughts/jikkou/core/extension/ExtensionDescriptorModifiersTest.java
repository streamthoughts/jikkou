/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionDescriptorModifiersTest {

    static final List<Example> NO_EXAMPLES = Collections.emptyList();
    static final List<ConfigPropertySpec> NO_PROPERTIES = Collections.emptyList();

    static final ExtensionDescriptor<ExtensionDescriptorModifiersTest> DESCRIPTOR = new DefaultExtensionDescriptor<>(
        ExtensionDescriptorModifiersTest.class.getName(),
        "",
        "",
        NO_EXAMPLES,
        ExtensionCategory.EXTENSION,
        NO_PROPERTIES,
        null,
        () -> null,
        ExtensionDescriptorModifiersTest.class,
        ExtensionDescriptorModifiersTest.class.getClassLoader(),
        () -> null,
        null,
        false,
        null
    );

    @Test
    void shouldModifyDecoratorName() {

        ExtensionDescriptorModifier modifier = ExtensionDescriptorModifiers.withName("modified");
        ExtensionDescriptor<ExtensionDescriptorModifiersTest> result = modifier.apply(DESCRIPTOR);
        Assertions.assertEquals("modified", result.name());
    }

    @Test
    void shouldModifyDecoratorIsEnabled() {
        ExtensionDescriptorModifier modifier = ExtensionDescriptorModifiers.enabled(true);
        ExtensionDescriptor<ExtensionDescriptorModifiersTest> result = modifier.apply(DESCRIPTOR);
        Assertions.assertTrue(result.isEnabled());
    }
}