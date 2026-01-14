/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultProviderConfigurationRegistryTest {

    private static final String PROVIDER_TYPE_KAFKA = "io.streamthoughts.jikkou.kafka.KafkaExtensionProvider";
    private static final String PROVIDER_TYPE_SCHEMA_REGISTRY = "io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider";

    private static final String PROVIDER_NAME_KAFKA_PROD = "kafka-prod";
    private static final String PROVIDER_NAME_KAFKA_DEV = "kafka-dev";
    private static final String PROVIDER_NAME_SCHEMA_REGISTRY = "schema-registry";

    private DefaultProviderConfigurationRegistry registry;

    @BeforeEach
    void beforeEach() {
        registry = new DefaultProviderConfigurationRegistry();
    }

    @Test
    void shouldRegisterProviderConfiguration() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");

        // When
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);

        // Then
        Assertions.assertTrue(registry.getProviderConfiguration(PROVIDER_NAME_KAFKA_PROD).isPresent());
        Assertions.assertEquals(config, registry.getProviderConfiguration(PROVIDER_NAME_KAFKA_PROD).get());
    }

    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateProviderName() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "localhost:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "localhost:9093");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, false);

        // When - Then
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config2, false));
        Assertions.assertEquals(
                "Configuration is already registered for provider name: " + PROVIDER_NAME_KAFKA_PROD,
                exception.getMessage());
    }

    @Test
    void shouldSetDefaultProviderWhenIsDefaultTrue() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");

        // When
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, true);

        // Then
        Optional<Configuration> defaultConfig = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);
        Assertions.assertTrue(defaultConfig.isPresent());
        Assertions.assertEquals(config, defaultConfig.get());
    }

    @Test
    void shouldReplaceDefaultProviderWhenNewDefaultRegistered() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "prod:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "dev:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, true);

        // When
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config2, true);

        // Then
        Optional<Configuration> defaultConfig = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);
        Assertions.assertTrue(defaultConfig.isPresent());
        Assertions.assertEquals(config2, defaultConfig.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenProviderConfigurationNotFound() {
        // When
        Optional<Configuration> result = registry.getProviderConfiguration("non-existent");

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoDefaultConfiguredAndMultipleProviders() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "prod:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "dev:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config2, false);

        // When
        Optional<Configuration> result = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnSingleProviderAsDefaultWhenNoExplicitDefault() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);

        // When
        Optional<Configuration> result = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(config, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoProvidersForType() {
        // When
        Optional<Configuration> result = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetConfigurationByProviderName() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);

        // When
        Configuration result = registry.getConfiguration(PROVIDER_TYPE_KAFKA, PROVIDER_NAME_KAFKA_PROD);

        // Then
        Assertions.assertEquals(config, result);
    }

    @Test
    void shouldGetDefaultConfigurationWhenProviderNameIsNull() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, true);

        // When
        Configuration result = registry.getConfiguration(PROVIDER_TYPE_KAFKA, null);

        // Then
        Assertions.assertEquals(config, result);
    }

    @Test
    void shouldGetDefaultConfigurationWhenProviderNameIsEmpty() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, true);

        // When
        Configuration result = registry.getConfiguration(PROVIDER_TYPE_KAFKA, "");

        // Then
        Assertions.assertEquals(config, result);
    }

    @Test
    void shouldThrowExceptionWhenNoDefaultProviderAndProviderNameIsNull() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "prod:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "dev:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config2, false);

        // When - Then
        JikkouRuntimeException exception = Assertions.assertThrows(
                JikkouRuntimeException.class, () -> registry.getConfiguration(PROVIDER_TYPE_KAFKA, null));
        Assertions.assertEquals(
                "No default configuration defined, and multiple configurations found for provider type: '" + PROVIDER_TYPE_KAFKA + "'", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProviderNameNotFound() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);

        // When - Then
        JikkouRuntimeException exception = Assertions.assertThrows(
                JikkouRuntimeException.class, () -> registry.getConfiguration(PROVIDER_TYPE_KAFKA, "non-existent"));
        Assertions.assertEquals("No provider configured for name: 'non-existent'", exception.getMessage());
    }

    @Test
    void shouldReturnEmptySetWhenNoProvidersRegistered() {
        // When
        Set<String> result = registry.getAllProviderNames();

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllProviderNames() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "prod:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "dev:9092");
        Configuration config3 = Configuration.of("schema.registry.url", "http://localhost:8081");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config2, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_SCHEMA_REGISTRY, PROVIDER_TYPE_SCHEMA_REGISTRY, config3, false);

        // When
        Set<String> result = registry.getAllProviderNames();

        // Then
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(PROVIDER_NAME_KAFKA_PROD));
        Assertions.assertTrue(result.contains(PROVIDER_NAME_KAFKA_DEV));
        Assertions.assertTrue(result.contains(PROVIDER_NAME_SCHEMA_REGISTRY));
    }

    @Test
    void shouldReturnEmptySetWhenNoProvidersForType() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);

        // When
        Set<String> result = registry.getProviderNamesByType(PROVIDER_TYPE_SCHEMA_REGISTRY);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnProviderNamesForSpecificType() {
        // Given
        Configuration config1 = Configuration.of("bootstrap.servers", "prod:9092");
        Configuration config2 = Configuration.of("bootstrap.servers", "dev:9092");
        Configuration config3 = Configuration.of("schema.registry.url", "http://localhost:8081");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config1, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config2, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_SCHEMA_REGISTRY, PROVIDER_TYPE_SCHEMA_REGISTRY, config3, false);

        // When
        Set<String> kafkaProviders = registry.getProviderNamesByType(PROVIDER_TYPE_KAFKA);

        // Then
        Assertions.assertEquals(2, kafkaProviders.size());
        Assertions.assertTrue(kafkaProviders.contains(PROVIDER_NAME_KAFKA_PROD));
        Assertions.assertTrue(kafkaProviders.contains(PROVIDER_NAME_KAFKA_DEV));
        Assertions.assertFalse(kafkaProviders.contains(PROVIDER_NAME_SCHEMA_REGISTRY));
    }

    @Test
    void shouldNotSetDefaultWhenIsDefaultFalse() {
        // Given
        Configuration config = Configuration.of("bootstrap.servers", "localhost:9092");

        // When
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, config, false);
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_DEV, PROVIDER_TYPE_KAFKA, config, false);

        // Then - getDefaultConfiguration should return empty since no explicit default and multiple providers
        Optional<Configuration> defaultConfig = registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA);
        Assertions.assertTrue(defaultConfig.isEmpty());
    }

    @Test
    void shouldHandleMultipleProviderTypes() {
        // Given
        Configuration kafkaConfig = Configuration.of("bootstrap.servers", "localhost:9092");
        Configuration schemaConfig = Configuration.of("schema.registry.url", "http://localhost:8081");
        registry.registerProviderConfiguration(PROVIDER_NAME_KAFKA_PROD, PROVIDER_TYPE_KAFKA, kafkaConfig, true);
        registry.registerProviderConfiguration(
                PROVIDER_NAME_SCHEMA_REGISTRY, PROVIDER_TYPE_SCHEMA_REGISTRY, schemaConfig, true);

        // When - Then
        Assertions.assertEquals(kafkaConfig, registry.getDefaultConfiguration(PROVIDER_TYPE_KAFKA).orElse(null));
        Assertions.assertEquals(
                schemaConfig, registry.getDefaultConfiguration(PROVIDER_TYPE_SCHEMA_REGISTRY).orElse(null));
    }
}