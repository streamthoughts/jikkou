/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProviderGroupRegistryTest {

    private ProviderGroupRegistry registry;

    @BeforeEach
    void beforeEach() {
        registry = new ProviderGroupRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveGroup() {
        // Given
        List<String> providers = List.of("kafka-prod", "kafka-prod-dr");

        // When
        registry.registerGroup("production", providers);

        // Then
        Assertions.assertEquals(providers, registry.getProviderNames("production"));
        Assertions.assertTrue(registry.hasGroup("production"));
    }

    @Test
    void shouldReturnAllGroupNames() {
        // Given
        registry.registerGroup("production", List.of("kafka-prod", "kafka-prod-dr"));
        registry.registerGroup("non-prod", List.of("kafka-staging", "kafka-dev"));

        // When
        Set<String> names = registry.getAllGroupNames();

        // Then
        Assertions.assertEquals(Set.of("production", "non-prod"), names);
    }

    @Test
    void shouldThrowWhenGroupNotFound() {
        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> registry.getProviderNames("unknown-group"));
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateGroup() {
        // Given
        registry.registerGroup("production", List.of("kafka-prod"));

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> registry.registerGroup("production", List.of("kafka-staging")));
    }

    @Test
    void shouldReturnFalseForNonExistentGroup() {
        Assertions.assertFalse(registry.hasGroup("nonexistent"));
    }

    @Test
    void shouldReturnImmutableProviderList() {
        // Given
        registry.registerGroup("production", List.of("kafka-prod"));

        // When
        List<String> providers = registry.getProviderNames("production");

        // Then
        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> providers.add("new-provider"));
    }
}
