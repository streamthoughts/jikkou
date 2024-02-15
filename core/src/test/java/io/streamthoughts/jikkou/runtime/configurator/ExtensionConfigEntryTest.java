/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.HasPriority;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionConfigEntryTest {

    @Test
    void shouldGetExtensionConfigGivenCompleteConfiguration(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigEntry.TYPE_CONFIG.key(), "Type",
                ExtensionConfigEntry.PRIORITY_CONFIG.key(), 100,
                ExtensionConfigEntry.NAME_CONFIG.key(), "Name",
                ExtensionConfigEntry.CONFIGURATION_CONFIG.key(), Map.of("k1", "v1")
        ));
        // When
        ExtensionConfigEntry extensionConfig = ExtensionConfigEntry.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.type());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(100, extensionConfig.priority());
        Assertions.assertEquals(Configuration.of("k1", "v1"), extensionConfig.config());
    }

    @Test
    void shouldGetExtensionConfigGivenConfigurationWithNoPriority(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigEntry.TYPE_CONFIG.key(), "Type",
                ExtensionConfigEntry.NAME_CONFIG.key(), "Name",
                ExtensionConfigEntry.CONFIGURATION_CONFIG.key(), Map.of("k1", "v1")
        ));
        // When
        ExtensionConfigEntry extensionConfig = ExtensionConfigEntry.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.type());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(HasPriority.NO_ORDER, extensionConfig.priority());
        Assertions.assertEquals(Configuration.of("k1", "v1"), extensionConfig.config());
    }

    @Test
    void shouldGetExtensionConfigGivenConfigurationWithNoConfig(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigEntry.TYPE_CONFIG.key(), "Type",
                ExtensionConfigEntry.NAME_CONFIG.key(), "Name",
                ExtensionConfigEntry.PRIORITY_CONFIG.key(), 100
        ));
        // When
        ExtensionConfigEntry extensionConfig = ExtensionConfigEntry.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.type());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(100, extensionConfig.priority());
        Assertions.assertEquals(Configuration.empty(), extensionConfig.config());
    }
}