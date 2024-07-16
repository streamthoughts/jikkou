/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.config.ConfigException;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.schema.registry.api.AuthMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryExtensionProviderTest {

    public static final String REGISTRY_URL = "http://localhost:8081";

    private final SchemaRegistryExtensionProvider provider = new SchemaRegistryExtensionProvider();


    @Test
    void shouldThrowExceptionForMissingSchemaRegistryUrl() {
        // Given
        Configuration configuration = Configuration.empty();

        // When - Then
        ConfigException.Missing missing = Assertions
                .assertThrowsExactly(ConfigException.Missing.class, () -> provider.schemaRegistryUrl.get(configuration));
        Assertions.assertEquals(provider.schemaRegistryUrl, missing.property());
    }

    @Test
    void shouldReturnPassedValueForSchemaRegistryUrl() {
        // Given
        Configuration configuration = provider.schemaRegistryUrl.asConfiguration(REGISTRY_URL);

        // When
        String registryUrl = provider.schemaRegistryUrl.get(configuration);

        // Then
        Assertions.assertEquals(REGISTRY_URL, registryUrl);
    }

    @Test
    void shouldReturnNoneValueForMissingSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = Configuration.empty();
        // When
        AuthMethod authMethod = provider.schemaRegistryAuthMethod.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.NONE, authMethod);
    }

    @Test
    void shouldReturnPassedValueForValidSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = provider.schemaRegistryAuthMethod.asConfiguration(AuthMethod.BASICAUTH.name());
        // When
        AuthMethod authMethod = provider.schemaRegistryAuthMethod.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.BASICAUTH, authMethod);
    }

    @Test
    void shouldReturnInvalidValueForInvalidSchemaRegistryAuthMethod() {
        // Given
        Configuration configuration = provider.schemaRegistryAuthMethod.asConfiguration("dummy");
        // When
        AuthMethod authMethod = provider.schemaRegistryAuthMethod.get(configuration);
        // Then
        Assertions.assertEquals(AuthMethod.INVALID, authMethod);
    }
}