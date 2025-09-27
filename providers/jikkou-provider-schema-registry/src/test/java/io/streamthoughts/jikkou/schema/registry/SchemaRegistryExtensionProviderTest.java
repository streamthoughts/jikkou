/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.config.ConfigException;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider.Config;
import io.streamthoughts.jikkou.schema.registry.api.AuthMethod;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryExtensionProviderTest {

    public static final String REGISTRY_URL = "http://localhost:8081";

    @Test
    void shouldThrowExceptionForMissingSchemaRegistryUrl() {
        // Given
        Configuration configuration = Configuration.empty();

        // When - Then
        ConfigException.Missing missing = Assertions
                .assertThrowsExactly(ConfigException.Missing.class, () -> Config.SCHEMA_REGISTRY_URL.get(configuration));
        Assertions.assertEquals(Config.SCHEMA_REGISTRY_URL, missing.property());
    }

    @Test
    void shouldReturnPassedValueForSchemaRegistryUrl() {
        // Given
        Configuration configuration = Config.SCHEMA_REGISTRY_URL.asConfiguration(REGISTRY_URL);

        // When
        String registryUrl = Config.SCHEMA_REGISTRY_URL.get(configuration);

        // Then
        Assertions.assertEquals(REGISTRY_URL, registryUrl);
    }

    @Test
    void shouldReturnNoneValueForMissingSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = Configuration.empty();
        // When
        AuthMethod authMethod =  Config.SCHEMA_REGISTRY_AUTH_METHOD.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.NONE, authMethod);
    }

    @Test
    void shouldReturnPassedValueForValidSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = Config.SCHEMA_REGISTRY_AUTH_METHOD.asConfiguration(AuthMethod.BASICAUTH.name());
        // When
        AuthMethod authMethod = Config.SCHEMA_REGISTRY_AUTH_METHOD.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.BASICAUTH, authMethod);
    }

    @Test
    void shouldReturnInvalidValueForInvalidSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = Config.SCHEMA_REGISTRY_AUTH_METHOD.asConfiguration("dummy");
        // When
        AuthMethod authMethod =  Config.SCHEMA_REGISTRY_AUTH_METHOD.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.INVALID, authMethod);
    }

    @Test
    void shouldRegisterExtensions() {
        // Given
        SchemaRegistryExtensionProvider provider = new SchemaRegistryExtensionProvider();
        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );

        // When
        provider.registerExtensions(registry);

        // Then
        Assertions.assertFalse(registry.findAllDescriptorsByClass(Transformation.class).isEmpty());
        Assertions.assertFalse(registry.findAllDescriptorsByClass(Controller.class).isEmpty());

        List<ExtensionDescriptor<Validation>> allSchemaValidationDescriptors = registry
                .findAllDescriptorsByClass(Validation.class,
                        Qualifiers.bySupportedResource(ResourceType.of(V1SchemaRegistrySubject.class)));
        Assertions.assertEquals(3, allSchemaValidationDescriptors.size());
    }
}