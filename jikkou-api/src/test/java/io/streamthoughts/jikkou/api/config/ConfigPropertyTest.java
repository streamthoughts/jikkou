/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigPropertyTest {

    public static final String KEY = "key";

    @Test
    void should_create_config_given_long_value() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY);
        // When
        Long value = config.evaluate(Configuration.from(Map.of(KEY, 42L)));
        // Then
        Assertions.assertEquals(42L, value);
    }

    @Test
    void should_create_config_given_long_default() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY).orElse(42L);
        // When
        Long value = config.evaluate(Configuration.empty());
        // Then
        Assertions.assertEquals(42L, value);
    }

    @Test
    void should_create_config_given_boolean_value() {
        // Given
        ConfigProperty<Boolean> config = ConfigProperty.ofBoolean(KEY);
        // When
        Boolean value = config.evaluate(Configuration.from(Map.of(KEY, true)));
        // Then
        Assertions.assertEquals(true, value);
    }

    @Test
    void should_create_config_given_boolean_default() {
        // Given
        ConfigProperty<Boolean> config = ConfigProperty.ofBoolean(KEY).orElse(true);
        // When
        Boolean value = config.evaluate(Configuration.empty());
        // Then
        Assertions.assertEquals(true, value);
    }

    @Test
    void should_create_config_given_int_value() {
        // Given
        ConfigProperty<Integer> config = ConfigProperty.ofInt(KEY);
        // When
        Integer value = config.evaluate(Configuration.from(Map.of(KEY, 42)));
        // Then
        Assertions.assertEquals(42, value);
    }

    @Test
    void should_create_config_given_int_default() {
        // Given
        ConfigProperty<Integer> config = ConfigProperty.ofInt(KEY).orElse(42);
        // When
        Integer value = config.evaluate(Configuration.empty());
        // Then
        Assertions.assertEquals(42, value);
    }

    @Test
    void should_create_config_given_string_value() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);
        // When
        String value = config.evaluate(Configuration.from(Map.of(KEY, "test")));
        // Then
        Assertions.assertEquals("test", value);
    }

    @Test
    void should_create_config_given_string_default() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY).orElse("test");

        // When
        String value = config.evaluate(Configuration.empty());

        // Then
        Assertions.assertEquals("test", value);
    }

    @Test
    void should_create_config_given_list_value() {
        // Given
        ConfigProperty<List<String>> config = ConfigProperty.ofList(KEY);
        // When
        List<String> value = config.evaluate(Configuration.from(Map.of(KEY, List.of("test"))));
        // Then
        Assertions.assertEquals(List.of("test"), value);
    }

    @Test
    void should_create_config_given_list_default() {
        // Given
        ConfigProperty<List<String>> config = ConfigProperty.ofList(KEY).orElse(List.of("test"));
        // When
        List<String> value = config.evaluate(Configuration.empty());
        // Then
        Assertions.assertEquals(List.of("test"), value);
    }

    @Test
    void should_throw_exception_given_no_value() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);

        // When, Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> config.evaluate(Configuration.empty()));
        Assertions.assertEquals("No value present for param 'key'", exception.getLocalizedMessage());
    }

    @Test
    void should_return_default_given_empty_config() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);
        // When
        String result = config.orElseGet(Configuration.empty(), () -> "default");
        // Then
        Assertions.assertEquals("default", result);
    }

    @Test
    void should_map_value_given_any_config() {
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
    void should_get_config_param_as_config() {
        // Given
        ConfigProperty<String> config = ConfigProperty.ofString(KEY);

        // When
        String input = "value";
        Configuration result = config.asConfiguration(input);
        // Then
        Assertions.assertEquals(input, result.getString(KEY));
    }

    @Test
    void should_create_config_given_class_value() {
        // Given
        ConfigProperty<List<Class<TestClass>>> config = ConfigProperty.ofClasses(KEY);
        // When
        List<Class<TestClass>> value = config.evaluate(Configuration.of(KEY, List.of(TestClass.class.getName())));
        // Then
        Assertions.assertEquals(1, value.size());
        Assertions.assertEquals(TestClass.class, value.get(0));
    }

    @Test
    void should_create_config_given_class_default() {
        // Given
        ConfigProperty<Long> config = ConfigProperty.ofLong(KEY).orElse(42L);
        // When
        Long value = config.evaluate(Configuration.empty());
        // Then
        Assertions.assertEquals(42L, value);
    }

    public static class TestClass {

        public TestClass() {
        }
    }


}