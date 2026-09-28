/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.core.config.Configuration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientHeadersConfigTest {

    @Test
    @DisplayName("Should return an empty map when no clientHeaders is configured")
    void shouldReturnEmptyMapWhenNotConfigured() {
        assertTrue(ClientHeadersConfig.from(Configuration.empty()).isEmpty());
    }

    @Test
    @DisplayName("Should read configured headers as strings")
    void shouldReadConfiguredHeaders() {
        // Given
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-Api-Key", "secret");
        headers.put("X-Tenant", "acme");
        Configuration configuration = Configuration.from(Map.of("clientHeaders", headers));

        // When
        Map<String, String> result = ClientHeadersConfig.from(configuration);

        // Then
        assertEquals(Map.of("X-Api-Key", "secret", "X-Tenant", "acme"), result);
    }

    @Test
    @DisplayName("Should coerce non-string header values to string")
    void shouldCoerceNonStringValues() {
        // Given
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-Retry-Count", 3);
        headers.put("X-Enabled", true);
        Configuration configuration = Configuration.from(Map.of("clientHeaders", headers));

        // When
        Map<String, String> result = ClientHeadersConfig.from(configuration);

        // Then
        assertEquals("3", result.get("X-Retry-Count"));
        assertEquals("true", result.get("X-Enabled"));
    }

    @Test
    @DisplayName("Should skip entries with a null value")
    void shouldSkipNullValues() {
        // Given
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Null", null);
        headers.put("X-Kept", "kept");
        Configuration configuration = Configuration.from(Map.of("clientHeaders", headers));

        // When
        Map<String, String> result = ClientHeadersConfig.from(configuration);

        // Then
        assertEquals(Map.of("X-Kept", "kept"), result);
    }

    @Test
    @DisplayName("Should trim surrounding whitespace from header names")
    void shouldTrimHeaderNames() {
        // Given
        Configuration configuration = Configuration.from(
                Map.of("clientHeaders", Map.of("  X-Padded  ", "value")));

        // When
        Map<String, String> result = ClientHeadersConfig.from(configuration);

        // Then
        assertEquals(Map.of("X-Padded", "value"), result);
    }

    @Test
    @DisplayName("Should return an unmodifiable map")
    void shouldReturnUnmodifiableMap() {
        // Given
        Configuration configuration = Configuration.from(
                Map.of("clientHeaders", Map.of("X-Kept", "kept")));

        // When
        Map<String, String> result = ClientHeadersConfig.from(configuration);

        // Then
        assertThrows(UnsupportedOperationException.class, () -> result.put("X-New", "nope"));
    }
}
