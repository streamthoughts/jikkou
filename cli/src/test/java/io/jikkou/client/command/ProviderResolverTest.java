/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.core.config.Configuration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProviderResolverTest {

    @Test
    void shouldResolveAllEnabledProviders() {
        Configuration config = Configuration.from(Map.of(
            "provider", Map.of(
                "kafka-prod", Map.of("type", "kafka", "enabled", true),
                "kafka-staging", Map.of("type", "kafka", "enabled", true),
                "kafka-disabled", Map.of("type", "kafka", "enabled", false)
            )
        ));

        ProviderResolver resolver = new ProviderResolver(config);
        List<String> providers = resolver.resolveAllProviders();

        assertEquals(2, providers.size());
        assertTrue(providers.contains("kafka-prod"));
        assertTrue(providers.contains("kafka-staging"));
    }

    @Test
    void shouldResolveProviderGroup() {
        Configuration config = Configuration.from(Map.of(
            "provider-groups", Map.of(
                "production", List.of("kafka-prod", "kafka-prod-dr")
            )
        ));

        ProviderResolver resolver = new ProviderResolver(config);
        List<String> providers = resolver.resolveProviderGroup("production");

        assertEquals(List.of("kafka-prod", "kafka-prod-dr"), providers);
    }

    @Test
    void shouldThrowWhenGroupNotFound() {
        Configuration config = Configuration.from(Map.of(
            "provider-groups", Map.of(
                "production", List.of("kafka-prod")
            )
        ));

        ProviderResolver resolver = new ProviderResolver(config);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> resolver.resolveProviderGroup("unknown-group")
        );
        assertTrue(exception.getMessage().contains("unknown-group"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void shouldReturnEmptyListWhenNoFlagsSet() {
        Configuration config = Configuration.from(Map.of());
        ProviderResolver resolver = new ProviderResolver(config);

        ProviderOptionMixin mixin = new ProviderOptionMixin();
        List<String> result = resolver.resolveProviderNames(mixin);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenOnlySingleProviderSet() {
        Configuration config = Configuration.from(Map.of());
        ProviderResolver resolver = new ProviderResolver(config);

        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.provider = "kafka-prod";
        List<String> result = resolver.resolveProviderNames(mixin);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldResolveProviderNamesWithProviderAll() {
        Configuration config = Configuration.from(Map.of(
            "provider", Map.of(
                "kafka-prod", Map.of("type", "kafka", "enabled", true),
                "kafka-staging", Map.of("type", "kafka", "enabled", true)
            )
        ));

        ProviderResolver resolver = new ProviderResolver(config);
        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.providerAll = true;

        List<String> result = resolver.resolveProviderNames(mixin);

        assertEquals(2, result.size());
        assertTrue(result.contains("kafka-prod"));
        assertTrue(result.contains("kafka-staging"));
    }

    @Test
    void shouldResolveProviderNamesWithProviderGroup() {
        Configuration config = Configuration.from(Map.of(
            "provider-groups", Map.of(
                "production", List.of("kafka-prod", "kafka-prod-dr")
            )
        ));

        ProviderResolver resolver = new ProviderResolver(config);
        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.providerGroup = "production";

        List<String> result = resolver.resolveProviderNames(mixin);

        assertEquals(List.of("kafka-prod", "kafka-prod-dr"), result);
    }

    @Test
    void shouldThrowWhenMultipleFlagsSet() {
        Configuration config = Configuration.from(Map.of());
        ProviderResolver resolver = new ProviderResolver(config);

        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.provider = "kafka-prod";
        mixin.providerAll = true;

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> resolver.resolveProviderNames(mixin)
        );
        assertTrue(exception.getMessage().contains("mutually exclusive"));
    }

    @Test
    void shouldThrowWhenProviderAndGroupSet() {
        Configuration config = Configuration.from(Map.of());
        ProviderResolver resolver = new ProviderResolver(config);

        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.provider = "kafka-prod";
        mixin.providerGroup = "production";

        assertThrows(IllegalArgumentException.class, () -> resolver.resolveProviderNames(mixin));
    }

    @Test
    void shouldThrowWhenAllAndGroupSet() {
        Configuration config = Configuration.from(Map.of());
        ProviderResolver resolver = new ProviderResolver(config);

        ProviderOptionMixin mixin = new ProviderOptionMixin();
        mixin.providerAll = true;
        mixin.providerGroup = "production";

        assertThrows(IllegalArgumentException.class, () -> resolver.resolveProviderNames(mixin));
    }
}
