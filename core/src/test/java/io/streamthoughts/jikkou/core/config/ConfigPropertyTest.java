/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigPropertyTest {

    public static final String KEY = "key";

    @Test
    void shouldCreateConfigGivenLongValue() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY);
        // When
        Long value = config.get(Configuration.from(Map.of(KEY, 42L)));
        // Then
        Assertions.assertEquals(42L, value);
    }

    @Test
    void shouldCreateConfigGivenLongDefault() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY).defaultValue(42L);
        // When
        Long value = config.get(Configuration.empty());
        // Then
        Assertions.assertEquals(42L, value);
    }

    @Test
    void shouldCreateConfigGivenBooleanValue() {
        // Given
        ConfigProperty<Boolean> config = ConfigProperty.ofBoolean(KEY);
        // When
        Boolean value = config.get(Configuration.from(Map.of(KEY, true)));
        // Then
        Assertions.assertEquals(true, value);
    }

    @Test
    void shouldCreateConfigGivenBooleanDefault() {
        // Given
        ConfigProperty<Boolean> config = ConfigProperty.ofBoolean(KEY).defaultValue(true);
        // When
        Boolean value = config.get(Configuration.empty());
        // Then
        Assertions.assertEquals(true, value);
    }

    @Test
    void shouldCreateConfigGivenIntValue() {
        // Given
        ConfigProperty<Integer> config = ConfigProperty.ofInt(KEY);
        // When
        Integer value = config.get(Configuration.from(Map.of(KEY, 42)));
        // Then
        Assertions.assertEquals(42, value);
    }

    @Test
    void shouldCreateConfigGivenIntDefault() {
        // Given
        ConfigProperty<Integer> config = ConfigProperty.ofInt(KEY).defaultValue(42);
        // When
        Integer value = config.get(Configuration.empty());
        // Then
        Assertions.assertEquals(42, value);
    }

    @Test
    void shouldCreateConfigGivenStringValue() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);
        // When
        String value = config.get(Configuration.from(Map.of(KEY, "test")));
        // Then
        Assertions.assertEquals("test", value);
    }

    @Test
    void shouldCreateConfigGivenStringDefault() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY).defaultValue("test");

        // When
        String value = config.get(Configuration.empty());

        // Then
        Assertions.assertEquals("test", value);
    }

    @Test
    void shouldCreateConfigGivenListValue() {
        // Given
        ConfigProperty<List<String>> config = ConfigProperty.ofList(KEY);
        // When
        List<String> value = config.get(Configuration.from(Map.of(KEY, List.of("test"))));
        // Then
        Assertions.assertEquals(List.of("test"), value);
    }

    @Test
    void shouldCreateConfigGivenListDefault() {
        // Given
        ConfigProperty<List<String>> config = ConfigProperty.ofList(KEY).defaultValue(List.of("test"));
        // When
        List<String> value = config.get(Configuration.empty());
        // Then
        Assertions.assertEquals(List.of("test"), value);
    }

    @Test
    void shouldThrowExceptionGivenNoValue() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);

        // When, Then
        ConfigException.Missing exception = assertThrows(ConfigException.Missing.class, () -> config.get(Configuration.empty()));
        Assertions.assertEquals(KEY, exception.property().key());
    }

    @Test
    void shouldReturnDefaultGivenEmptyConfig() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);
        // When
        String result = config.orElseGet(Configuration.empty(), () -> "default");
        // Then
        Assertions.assertEquals("default", result);
    }

    @Test
    void shouldMapValueGivenAnyConfig() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY)
                .map(String::toUpperCase);

        // When
        String input = "value";
        String result = config.orElseGet(Configuration.of(KEY, input), () -> input);
        // Then
        Assertions.assertEquals(input.toUpperCase(), result);
    }

    @Test
    void shouldGetConfigParamAsConfig() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);

        // When
        String input = "value";
        Configuration result = config.asConfiguration(input);
        // Then
        Assertions.assertEquals(input, result.getString(KEY));
    }

    @Test
    void shouldCreateConfigGivenClassValue() {
        // Given
        ConfigProperty<List<Class<TestClass>>> config = ConfigProperty.ofClasses(KEY);
        // When
        List<Class<TestClass>> value = config.get(Configuration.of(KEY, List.of(TestClass.class.getName())));
        // Then
        Assertions.assertEquals(1, value.size());
        Assertions.assertEquals(TestClass.class, value.get(0));
    }

    @Test
    void shouldCreateConfigGivenClassDefault() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY).defaultValue(42L);
        // When
        Long value = config.get(Configuration.empty());
        // Then
        Assertions.assertEquals(42L, value);
    }

    @Test
    void shouldGetConfigListGivenDefault() {
        // Given
        ConfigProperty<List<Configuration>> config = ConfigProperty
                .ofConfigList(KEY)
                .defaultValue(List.of(Configuration.of("k1", "v1")));
        // When
        List<Configuration> value = config.get(Configuration.empty());
        // Then
        Assertions.assertNotNull(value);
        Assertions.assertEquals(1, value.size());
    }

    @Test
    void shouldGetConfigListGivenValue() {
        // Given
        ConfigProperty<List<Configuration>> config = ConfigProperty.ofConfigList(KEY);
        // When
        List<Configuration> value = config.get(Configuration.of(KEY, List.of(Map.of("k1", "v1"))));
        // Then
        Assertions.assertNotNull(value);
        Assertions.assertEquals(1, value.size());
    }

    @Test
    void shouldReturnTrueGivenEqualsConfig() {
        ConfigProperty<String> p1 = ConfigProperty.ofString("key")
                .description("key")
                .description("default");

        ConfigProperty<String> p2 = ConfigProperty.ofString("key")
                .description("key")
                .description("default");

        Assertions.assertEquals(p1, p2);
    }

    public static class TestClass {

        public TestClass() {
        }
    }


}