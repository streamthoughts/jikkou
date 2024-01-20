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

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassExtensionAliasesGeneratorTest {


    @Test
    void shouldGetClassAliasesForNameSuffixedWithTransformation() {
        ClassExtensionAliasesGenerator generator = new ClassExtensionAliasesGenerator();
        ExtensionDescriptor<TestTransformation> descriptor = getDescriptor(TestTransformation.class);
        Set<String> aliases = generator.getAliasesFor(descriptor, Collections.emptyList());
        Assertions.assertEquals(Set.of("Test", "TestTransformation"), aliases);
    }

    @Test
    void shouldGetClassAliasesForNameSuffixedWithValidation() {
        ClassExtensionAliasesGenerator generator = new ClassExtensionAliasesGenerator();
        ExtensionDescriptor<TestValidation> descriptor = getDescriptor(TestValidation.class);
        Set<String> aliases = generator.getAliasesFor(descriptor, Collections.emptyList());
        Assertions.assertEquals(Set.of("Test", "TestValidation"), aliases);
    }

    @Test
    void shouldGetClassAliasesForNameSuffixedWithExtension() {
        ClassExtensionAliasesGenerator generator = new ClassExtensionAliasesGenerator();
        ExtensionDescriptor<TestExtension> descriptor = getDescriptor(TestExtension.class);
        Set<String> aliases = generator.getAliasesFor(descriptor, Collections.emptyList());
        Assertions.assertEquals(Set.of("Test", "TestExtension"), aliases);
    }

    @Test
    void shouldGetUniqueClassAliases() {
        ClassExtensionAliasesGenerator generator = new ClassExtensionAliasesGenerator();
        List<ExtensionDescriptor<?>> descriptors = List.of(
                getDescriptor(TestValidation.class),
                getDescriptor(TestValidation.class)
        );
        Set<String> aliases = generator.getAliasesFor(
                descriptors.get(0),
                descriptors);
        Assertions.assertTrue(aliases.isEmpty());
    }

    private static <T> ExtensionDescriptor<T> getDescriptor(Class<T> clazz) {
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
                true
        );
    }

    private static abstract class TestTransformation implements Transformation<HasMetadata> {
    }

    private static abstract class TestValidation implements Validation<HasMetadata> {
    }

    private static abstract class TestExtension implements Extension {
    }

}