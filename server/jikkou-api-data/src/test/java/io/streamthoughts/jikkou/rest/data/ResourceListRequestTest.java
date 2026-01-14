/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceListRequestTest {

    @Test
    void shouldGetEmptyForNoArgs() {
        ResourceListRequest request = new ResourceListRequest();
        Assertions.assertNotNull(request.options());
        Assertions.assertNotNull(request.selectors());
        Assertions.assertNull(request.provider());
    }

    @Test
    void shouldCreateRequestWithProvider() {
        // Given
        String providerName = "kafka-prod";

        // When
        ResourceListRequest request = new ResourceListRequest(
                Map.of("key", "value"),
                List.of("selector1"),
                SelectorMatchingStrategy.ALL,
                providerName
        );

        // Then
        Assertions.assertEquals(providerName, request.provider());
        Assertions.assertEquals(Map.of("key", "value"), request.options());
        Assertions.assertEquals(List.of("selector1"), request.selectors());
        Assertions.assertEquals(SelectorMatchingStrategy.ALL, request.selectorMatchingStrategy());
    }

    @Test
    void shouldCreateRequestWithNullProvider() {
        // When
        ResourceListRequest request = new ResourceListRequest(
                Map.of("key", "value"),
                List.of("selector1"),
                SelectorMatchingStrategy.ALL,
                null
        );

        // Then
        Assertions.assertNull(request.provider());
    }

    @Test
    void shouldCreateRequestWithOptionsOnly() {
        // Given
        Map<String, Object> options = Map.of("key", "value");

        // When
        ResourceListRequest request = new ResourceListRequest(options);

        // Then
        Assertions.assertEquals(options, request.options());
        Assertions.assertTrue(request.selectors().isEmpty());
        Assertions.assertEquals(SelectorMatchingStrategy.ALL, request.selectorMatchingStrategy());
        Assertions.assertNull(request.provider());
    }

    @Test
    void shouldCreateRequestWithSelectorsButNoProvider() {
        // When
        ResourceListRequest request = new ResourceListRequest(
                Map.of("key", "value"),
                List.of("selector1", "selector2"),
                SelectorMatchingStrategy.ANY
        );

        // Then
        Assertions.assertEquals(Map.of("key", "value"), request.options());
        Assertions.assertEquals(List.of("selector1", "selector2"), request.selectors());
        Assertions.assertEquals(SelectorMatchingStrategy.ANY, request.selectorMatchingStrategy());
        Assertions.assertNull(request.provider());
    }

    @Test
    void shouldPreserveProviderWhenAddingOptions() {
        // Given
        ResourceListRequest original = new ResourceListRequest(
                Map.of("key1", "value1"),
                List.of("selector1"),
                SelectorMatchingStrategy.ALL,
                "kafka-prod"
        );

        // When
        ResourceListRequest updated = original.options(Map.of("key2", "value2"));

        // Then
        Assertions.assertEquals("kafka-prod", updated.provider());
        Assertions.assertEquals("value1", updated.options().get("key1"));
        Assertions.assertEquals("value2", updated.options().get("key2"));
    }

    @Test
    void shouldReturnDefaultSelectorMatchingStrategyWhenNull() {
        // When
        ResourceListRequest request = new ResourceListRequest(
                Collections.emptyMap(),
                Collections.emptyList(),
                null,
                "provider"
        );

        // Then
        Assertions.assertEquals(SelectorMatchingStrategy.ALL, request.selectorMatchingStrategy());
    }

    @Test
    void shouldReturnEmptyListWhenSelectorsNull() {
        // When
        ResourceListRequest request = new ResourceListRequest(
                Collections.emptyMap(),
                null,
                SelectorMatchingStrategy.ALL,
                "provider"
        );

        // Then
        Assertions.assertTrue(request.selectors().isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenOptionsNull() {
        // When
        ResourceListRequest request = new ResourceListRequest(
                null,
                Collections.emptyList(),
                SelectorMatchingStrategy.ALL,
                "provider"
        );

        // Then
        Assertions.assertTrue(request.options().isEmpty());
    }
}