/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.adapters;

import io.micronaut.http.HttpParameters;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HttpParametersAdapterTest {

    @Test
    void shouldReturnEmptyMapWhenNoParameters() {
        // Given
        HttpParameters parameters = Mockito.mock(HttpParameters.class);
        Mockito.when(parameters.names()).thenReturn(Collections.emptySet());

        // When
        Map<String, Object> result = HttpParametersAdapter.toMap(parameters);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnStringValueWhenSingleValue() {
        // Given
        HttpParameters parameters = Mockito.mock(HttpParameters.class);
        Mockito.when(parameters.names()).thenReturn(Set.of("key"));
        Mockito.when(parameters.getAll("key")).thenReturn(List.of("value"));

        // When
        Map<String, Object> result = HttpParametersAdapter.toMap(parameters);

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("value", result.get("key"));
    }

    @Test
    void shouldReturnListOfValuesWhenMultipleValues() {
        // Given
        HttpParameters parameters = Mockito.mock(HttpParameters.class);
        Mockito.when(parameters.names()).thenReturn(Set.of("key"));
        Mockito.when(parameters.getAll("key")).thenReturn(List.of("val1", "val2", "val3"));

        // When
        Map<String, Object> result = HttpParametersAdapter.toMap(parameters);

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(List.of("val1", "val2", "val3"), result.get("key"));
    }

    @Test
    void shouldReturnNullValueWhenParameterHasNoValues() {
        // Given
        HttpParameters parameters = Mockito.mock(HttpParameters.class);
        Mockito.when(parameters.names()).thenReturn(Set.of("key"));
        Mockito.when(parameters.getAll("key")).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = HttpParametersAdapter.toMap(parameters);

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey("key"));
        Assertions.assertNull(result.get("key"));
    }

    @Test
    void shouldReturnConfigurationWhenToConfigurationCalled() {
        // Given
        HttpParameters parameters = Mockito.mock(HttpParameters.class);
        Mockito.when(parameters.names()).thenReturn(Set.of("option"));
        Mockito.when(parameters.getAll("option")).thenReturn(List.of("enabled"));

        // When
        Configuration config = HttpParametersAdapter.toConfiguration(parameters);

        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals("enabled", config.getString("option"));
    }
}
