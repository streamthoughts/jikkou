/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.model.HasPriority;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionConfigTest {

    @Test
    void shouldGetExtensionConfigGivenCompleteConfiguration(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigDescriptor.TYPE_CONFIG.key(), "Type",
                ExtensionConfigDescriptor.PRIORITY_CONFIG.key(), 100,
                ExtensionConfigDescriptor.NAME_CONFIG.key(), "Name",
                ExtensionConfigDescriptor.CONFIGURATION_CONFIG.key(), Configuration.of("k1", "v1")
        ));
        // When
        ExtensionConfigDescriptor extensionConfig = ExtensionConfigDescriptor.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.extensionClass());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(100, extensionConfig.priority());
        Assertions.assertEquals(Configuration.of("k1", "v1"), extensionConfig.config());
    }

    @Test
    void shouldGetExtensionConfigGivenConfigurationWithNoPriority(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigDescriptor.TYPE_CONFIG.key(), "Type",
                ExtensionConfigDescriptor.NAME_CONFIG.key(), "Name",
                ExtensionConfigDescriptor.CONFIGURATION_CONFIG.key(), Configuration.of("k1", "v1")
        ));
        // When
        ExtensionConfigDescriptor extensionConfig = ExtensionConfigDescriptor.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.extensionClass());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(HasPriority.NO_ORDER, extensionConfig.priority());
        Assertions.assertEquals(Configuration.of("k1", "v1"), extensionConfig.config());
    }

    @Test
    void shouldGetExtensionConfigGivenConfigurationWithNoConfig(){
        // Given
        Configuration configuration = Configuration.from(Map.of(
                ExtensionConfigDescriptor.TYPE_CONFIG.key(), "Type",
                ExtensionConfigDescriptor.NAME_CONFIG.key(), "Name",
                ExtensionConfigDescriptor.PRIORITY_CONFIG.key(), 100
        ));
        // When
        ExtensionConfigDescriptor extensionConfig = ExtensionConfigDescriptor.of(configuration);

        // Then
        Assertions.assertEquals("Type", extensionConfig.extensionClass());
        Assertions.assertEquals("Name", extensionConfig.name());
        Assertions.assertEquals(100, extensionConfig.priority());
        Assertions.assertEquals(Configuration.empty(), extensionConfig.config());
    }
}