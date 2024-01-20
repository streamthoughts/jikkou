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

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionRegistrationException;
import io.streamthoughts.jikkou.core.extension.exceptions.NoUniqueExtensionException;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultExtensionRegistryTest {

    private DefaultExtensionRegistry registry;

    @BeforeEach
    void beforeEach() {
        registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
    }

    @Test
    void shouldFindDescriptorByClassForRegisteredExtension() {
        // Given
        registry.register(DefaultExtensionRegistryTest.class, DefaultExtensionRegistryTest::new);

        // When
        var optional = registry.findDescriptorByClass(DefaultExtensionRegistryTest.class);
        Assertions.assertTrue(optional.isPresent());
    }

    @Test
    void shouldFindDescriptorByAliasForRegisteredExtension() {
        // Given
        registry.register(DefaultExtensionRegistryTest.class, DefaultExtensionRegistryTest::new);

        // When
        var optional = registry.findDescriptorByAlias("DefaultExtensionRegistryTest");
        Assertions.assertTrue(optional.isPresent());
    }

    @Test
    void shouldFindingAllDescriptorsByClass() {
        // Given
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name1")
        );
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name2")
        );

        // When
        List<ExtensionDescriptor<DefaultExtensionRegistryTest>> descriptors = registry
                .findAllDescriptorsByClass(DefaultExtensionRegistryTest.class);

        Assertions.assertEquals(2, descriptors.size());
    }

    @Test
    void shouldThrowNoUniqueExtensionExceptionWhenFindingDescriptorWithNoQualifier() {
        // Given
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name1")
        );
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name2")
        );

        // When - Then
        Assertions.assertThrows(
                NoUniqueExtensionException.class,
                () -> registry.findDescriptorByClass(DefaultExtensionRegistryTest.class)
        );
    }

    @Test
    void shouldGetDescriptorGivenQualifier() {
        // Given
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name1")
        );
        registry.register(
                DefaultExtensionRegistryTest.class,
                DefaultExtensionRegistryTest::new,
                ExtensionDescriptorModifiers.withName("name2")
        );

        // When - Then
        Optional<ExtensionDescriptor<DefaultExtensionRegistryTest>> optional = registry
                .findDescriptorByClass(DefaultExtensionRegistryTest.class, Qualifiers.byName("name2"));
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals("name2", optional.get().name());
    }

    @Test
    void shouldThrowConflictingExtensionDefinitionExceptionForDuplicateRegistration() {
        // Given
        registry.register(DefaultExtensionRegistryTest.class, DefaultExtensionRegistryTest::new);

        // When
        Assertions.assertThrows(
                ConflictingExtensionDefinitionException.class,
                () -> registry.register(DefaultExtensionRegistryTest.class, DefaultExtensionRegistryTest::new)
        );
    }

    @Test
    void shouldThrowExtensionRegistrationExceptionForDescriptorWithNameNull() {
        // Given
        var descriptor = Mockito.mock(ExtensionDescriptor.class);
        Mockito.when(descriptor.toString()).thenReturn("");
        // When
        ExtensionRegistrationException e = Assertions.assertThrows(
                ExtensionRegistrationException.class,
                () ->  registry.registerDescriptor(descriptor));
        Assertions.assertEquals("Cannot register extension with name 'null': ", e.getMessage());
    }

    @Test
    void shouldThrowExtensionRegistrationExceptionForDescriptorWithTypeNull() {
        // Given
        var descriptor = Mockito.mock(ExtensionDescriptor.class);
        Mockito.when(descriptor.name()).thenReturn("test");
        Mockito.when(descriptor.toString()).thenReturn("");
        // When
        ExtensionRegistrationException e = assertThrows(
                ExtensionRegistrationException.class,
                () -> registry.registerDescriptor(descriptor));
        Assertions.assertEquals("Cannot register extension with type 'null': ", e.getMessage());
    }
}