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
            "",
            ExtensionDescriptorModifiersTest.class,
            ExtensionDescriptorModifiersTest.class.getClassLoader(),
            () -> null,
            false
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