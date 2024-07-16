/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
            null,
            () -> null,
            clazz,
            clazz.getClassLoader(),
            () -> null,
            null,
            true,
            null
        );
    }

    private static abstract class TestTransformation implements Transformation<HasMetadata> {
    }

    private static abstract class TestValidation implements Validation<HasMetadata> {
    }

    private static abstract class TestExtension implements Extension {
    }

}