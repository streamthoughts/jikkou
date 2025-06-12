/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class JikkouConfigProperties {

    @Deprecated
    public static final String EXTENSION_PROVIDER_CONFIG_PREFIX = "extension.providers";

    @Deprecated
    public static final ConfigProperty<Boolean> EXTENSIONS_PROVIDER_DEFAULT_ENABLED = ConfigProperty
        .ofBoolean(EXTENSION_PROVIDER_CONFIG_PREFIX + ".default.enabled")
        .description("Specify whether all extension providers should be enabled or disabled by default.")
        .defaultValue(true);

    public static final ConfigProperty<List<String>> EXTENSION_PATHS = ConfigProperty
        .ofList("extension.paths")
        .description("A list of directories from which to load external resource and extensions (controller, collector, transformation, validation, etc.")
        .defaultValue(Collections.emptyList());

    public static final ConfigProperty<List<ExtensionConfigEntry>> PROVIDER_CONFIG = ConfigProperty
        .ofConfig( "provider")
        .description( "The extension providers.")
        .map(config -> config.asMap(false).keySet()
            .stream()
            .map(key -> ExtensionConfigEntry.of(config.getConfig(key), key))
            .toList()
        )
        .defaultValue(List.of());

    public static final ConfigProperty<List<ExtensionConfigEntry>> VALIDATIONS_CONFIG = createExtensionConfig(
        "validations", "The list of custom validations to apply on resources.");

    public static final ConfigProperty<List<ExtensionConfigEntry>> TRANSFORMATION_CONFIG = createExtensionConfig(
        "transformations", "The list of custom transformations to apply on resources.");

    public static final ConfigProperty<List<ExtensionConfigEntry>> REPORTERS_CONFIG = createExtensionConfig(
        "reporters", "The list of custom reporters.");

    public static final ConfigProperty<List<ExtensionConfigEntry>> REPOSITORIES_CONFIG = createExtensionConfig(
        "repositories", "The list of resource repositories.");

    @NotNull
    private static ConfigProperty<List<ExtensionConfigEntry>> createExtensionConfig(final String configKey,
                                                                                    final String configDescription) {
        return ConfigProperty
            .ofConfigList(configKey)
            .description(configDescription)
            .map(configs -> configs.stream().map(ExtensionConfigEntry::of).collect(Collectors.toList()))
            .defaultValue(List.of());
    }

}
