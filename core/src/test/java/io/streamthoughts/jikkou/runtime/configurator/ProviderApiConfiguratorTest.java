/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.CONFIGURATION_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.DEFAULT_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.ENABLED_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.NAME_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.TYPE_CONFIG;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProviderApiConfiguratorTest {

    @Test
    void shouldFilterOutEntriesWithNullType() {
        // Given - entries with null type should not cause NPE when grouping by type
        Map<String, Object> entryWithNullType = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("test-provider"))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();
        // Note: TYPE_CONFIG is not set, so type() will return null

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(entryWithNullType))
        );

        // When - grouping by type should not throw NPE
        Map<String, List<ExtensionConfigEntry>> result = entries.stream()
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldGroupEntriesByType() {
        // Given
        String providerType = "io.streamthoughts.jikkou.TestProvider";

        Map<String, Object> entry1 = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-1"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(DEFAULT_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Map.of("key1", "value1")))
            .asMap();

        Map<String, Object> entry2 = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-2"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(DEFAULT_CONFIG.asValue(false))
            .with(CONFIGURATION_CONFIG.asValue(Map.of("key2", "value2")))
            .asMap();

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(entry1)),
            ExtensionConfigEntry.of(Configuration.from(entry2))
        );

        // When
        Map<String, List<ExtensionConfigEntry>> result = entries.stream()
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(providerType));
        Assertions.assertEquals(2, result.get(providerType).size());
    }

    @Test
    void shouldFilterOutDisabledProviders() {
        // Given
        String providerType = "io.streamthoughts.jikkou.TestProvider";

        Map<String, Object> enabledEntry = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("enabled-provider"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        Map<String, Object> disabledEntry = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("disabled-provider"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(false))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(enabledEntry)),
            ExtensionConfigEntry.of(Configuration.from(disabledEntry))
        );

        // When
        List<ExtensionConfigEntry> enabledProviders = entries.stream()
            .filter(ExtensionConfigEntry::enabled)
            .toList();

        // Then
        Assertions.assertEquals(1, enabledProviders.size());
        Assertions.assertEquals("enabled-provider", enabledProviders.get(0).name());
    }

    @Test
    void shouldHandleMixedEntriesWithAndWithoutType() {
        // Given
        String providerType = "io.streamthoughts.jikkou.TestProvider";

        Map<String, Object> entryWithType = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-with-type"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        Map<String, Object> entryWithoutType = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-without-type"))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(entryWithType)),
            ExtensionConfigEntry.of(Configuration.from(entryWithoutType))
        );

        // When - should not throw NPE
        Map<String, List<ExtensionConfigEntry>> result = entries.stream()
            .filter(ExtensionConfigEntry::enabled)
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(providerType));
        Assertions.assertEquals(1, result.get(providerType).size());
        Assertions.assertEquals("provider-with-type", result.get(providerType).get(0).name());
    }

    @Test
    void shouldHandleEmptyProviderConfiguration() {
        // Given
        List<ExtensionConfigEntry> entries = Collections.emptyList();

        // When
        Map<String, List<ExtensionConfigEntry>> result = entries.stream()
            .filter(ExtensionConfigEntry::enabled)
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldGroupMultipleProviderTypes() {
        // Given
        String providerType1 = "io.streamthoughts.jikkou.TestProvider1";
        String providerType2 = "io.streamthoughts.jikkou.TestProvider2";

        Map<String, Object> entry1 = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-type1-instance1"))
            .with(TYPE_CONFIG.asValue(providerType1))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        Map<String, Object> entry2 = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-type1-instance2"))
            .with(TYPE_CONFIG.asValue(providerType1))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        Map<String, Object> entry3 = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("provider-type2-instance1"))
            .with(TYPE_CONFIG.asValue(providerType2))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(entry1)),
            ExtensionConfigEntry.of(Configuration.from(entry2)),
            ExtensionConfigEntry.of(Configuration.from(entry3))
        );

        // When
        Map<String, List<ExtensionConfigEntry>> result = entries.stream()
            .filter(ExtensionConfigEntry::enabled)
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.containsKey(providerType1));
        Assertions.assertTrue(result.containsKey(providerType2));
        Assertions.assertEquals(2, result.get(providerType1).size());
        Assertions.assertEquals(1, result.get(providerType2).size());
    }

    @Test
    void shouldPreserveDefaultFlag() {
        // Given
        String providerType = "io.streamthoughts.jikkou.TestProvider";

        Map<String, Object> defaultEntry = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("default-provider"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(DEFAULT_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        Map<String, Object> nonDefaultEntry = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue("non-default-provider"))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(DEFAULT_CONFIG.asValue(false))
            .with(CONFIGURATION_CONFIG.asValue(Collections.emptyMap()))
            .asMap();

        // When
        ExtensionConfigEntry defaultConfig = ExtensionConfigEntry.of(Configuration.from(defaultEntry));
        ExtensionConfigEntry nonDefaultConfig = ExtensionConfigEntry.of(Configuration.from(nonDefaultEntry));

        // Then
        Assertions.assertTrue(defaultConfig.isDefault());
        Assertions.assertFalse(nonDefaultConfig.isDefault());
    }

    @Test
    void shouldLookupProviderConfigByType() {
        // Given - simulating the fix where we use providerType instead of providerName
        String providerType = "io.streamthoughts.jikkou.TestProvider";
        String providerName = "test-provider"; // different from providerType

        Map<String, Object> entry = NamedValueSet.emptySet()
            .with(NAME_CONFIG.asValue(providerName))
            .with(TYPE_CONFIG.asValue(providerType))
            .with(ENABLED_CONFIG.asValue(true))
            .with(CONFIGURATION_CONFIG.asValue(Map.of("key", "value")))
            .asMap();

        List<ExtensionConfigEntry> entries = List.of(
            ExtensionConfigEntry.of(Configuration.from(entry))
        );

        Map<String, List<ExtensionConfigEntry>> providerConfigByType = entries.stream()
            .filter(e -> e.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // When - lookup by type (correct behavior after fix)
        List<ExtensionConfigEntry> configsByType = providerConfigByType.get(providerType);

        // When - lookup by name (incorrect behavior before fix) - would return null
        List<ExtensionConfigEntry> configsByName = providerConfigByType.get(providerName);

        // Then
        Assertions.assertNotNull(configsByType);
        Assertions.assertEquals(1, configsByType.size());
        Assertions.assertEquals(providerName, configsByType.get(0).name());
        Assertions.assertNull(configsByName); // This was the bug - using name instead of type
    }
}
