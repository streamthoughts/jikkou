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
                "Provider",
                clazz,
                clazz.getClassLoader(),
                () -> null,
                isEnabled
        );
    }
}