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