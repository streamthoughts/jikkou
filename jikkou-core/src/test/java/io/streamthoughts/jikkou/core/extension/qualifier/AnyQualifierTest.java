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
                "",
                "",
                Collections.emptyList(),
                ExtensionCategory.EXTENSION,
                "",
                clazz,
                clazz.getClassLoader(),
                () -> null,
                isEnabled
        );
    }
}