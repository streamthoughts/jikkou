/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.runtime;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JikkouConfigTest {

    @Test
    void shouldLoadEmptyConfigurationByDefault() {
        // Given
        JikkouConfig config = JikkouConfig.load();
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(Collections.emptySet(), config.keys());
    }

    @Test
    void shouldCreateEmptyConfiguration() {
        // Given
        JikkouConfig config = JikkouConfig.empty();
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(Collections.emptySet(), config.keys());
    }

    @Test
    void shouldCreateConfigurationFromMap() {
        // Given
        Map<String, String> map = Map.of("prop1", "value1", "prop2", "value2");
        // When
        JikkouConfig config = JikkouConfig.create(map, false);
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(map, config.asMap());
    }

    @Test
    void shouldRedactSensitiveValues_whenToPrettyString() {
        // Given
        Map<String, String> map = Map.of(
                "password", "secret123",
                "basicAuthPassword", "xyz",
                "host", "localhost",
                "url", "http://localhost:8081");
        JikkouConfig config = JikkouConfig.create(map, false);

        // When
        String pretty = config.toPrettyString();

        // Then
        Assertions.assertFalse(pretty.contains("secret123"));
        Assertions.assertFalse(pretty.contains("xyz"));
        Assertions.assertTrue(pretty.contains("password = ******"));
        Assertions.assertTrue(pretty.contains("basicAuthPassword = ******"));
    }

    @Test
    void shouldRenderNonSensitiveValuesAndPreserveKeys_whenToPrettyString() {
        // Given
        Map<String, String> map = Map.of(
                "password", "secret123",
                "host", "localhost",
                "url", "http://localhost:8081");
        JikkouConfig config = JikkouConfig.create(map, false);

        // When
        String pretty = config.toPrettyString();

        // Then
        Assertions.assertTrue(pretty.contains("host = localhost"));
        Assertions.assertTrue(pretty.contains("url = http://localhost:8081"));
        Assertions.assertTrue(pretty.contains("password"));
    }
}