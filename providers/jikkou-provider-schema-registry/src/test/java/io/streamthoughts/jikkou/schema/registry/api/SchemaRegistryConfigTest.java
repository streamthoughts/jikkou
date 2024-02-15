/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.core.config.ConfigException;
import io.streamthoughts.jikkou.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryConfigTest {

    public static final String REGISTRY_URL = "http://localhost:8081";

    @Test
    void shouldThrowExceptionForMissingSchemaRegistryUrl() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(Configuration.empty());
        // When - Then
        ConfigException.Missing missing = Assertions
                .assertThrowsExactly(ConfigException.Missing.class, config::getSchemaRegistryUrl);
        Assertions.assertEquals(SchemaRegistryClientConfig.SCHEMA_REGISTRY_URL, missing.property());
    }

    @Test
    void shouldReturnPassedValueForSchemaRegistryUrl() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                SchemaRegistryClientConfig.SCHEMA_REGISTRY_URL.asConfiguration(REGISTRY_URL)
        );
        // When
        String registryUrl = config.getSchemaRegistryUrl();

        // Then
        Assertions.assertEquals(REGISTRY_URL, registryUrl);
    }

    @Test
    void shouldReturnNoneValueForMissingSchemaRegistryAuthMethod() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(Configuration.empty());
        // When
        AuthMethod authMethod = config.getAuthMethod();
        // Then
        Assertions.assertEquals(AuthMethod.NONE, authMethod);
    }

    @Test
    void shouldReturnPassedValueForValidSchemaRegistryAuthMethod() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                SchemaRegistryClientConfig.SCHEMA_REGISTRY_AUTH_METHOD.asConfiguration(AuthMethod.BASICAUTH.name())
        );
        // When
        AuthMethod authMethod = config.getAuthMethod();
        // Then
        Assertions.assertEquals(AuthMethod.BASICAUTH, authMethod);
    }

    @Test
    void shouldReturnInvalidValueForInvalidSchemaRegistryAuthMethod() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                SchemaRegistryClientConfig.SCHEMA_REGISTRY_AUTH_METHOD.asConfiguration("dummy")
        );
        // When
        AuthMethod authMethod = config.getAuthMethod();
        // Then
        Assertions.assertEquals(AuthMethod.INVALID, authMethod);
    }
}