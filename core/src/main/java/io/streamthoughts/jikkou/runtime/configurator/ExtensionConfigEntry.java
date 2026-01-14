/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.HasPriority;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an extension configuration.
 *
 * @param name     the name of the extension. Must not be null.
 * @param type     the class of the extension (or alias). Must not be null.
 * @param priority the extension priority. May be {@code null}.
 * @param enabled  Specifies whether the extension is enabled or disabled.
 * @param config   the extension configuration. May be {@code null}.
 */
public record ExtensionConfigEntry(String name,
                                   String type,
                                   Integer priority,
                                   Boolean enabled,
                                   Boolean isDefault,
                                   Configuration config) {

    public static final ConfigProperty<String> NAME_CONFIG = ConfigProperty
        .ofString("name")
        .description("The name of the configured extension")
        .required(false);

    public static final ConfigProperty<String> TYPE_CONFIG = ConfigProperty
        .ofString("type")
        .description("The type or fully qualified class name of the extension")
        .required(false);

    public static final ConfigProperty<Integer> PRIORITY_CONFIG = ConfigProperty
        .ofInt("priority")
        .description("The priority order of the extension")
        .defaultValue(HasPriority.NO_ORDER);

    public static final ConfigProperty<Boolean> ENABLED_CONFIG = ConfigProperty
        .ofBoolean("enabled")
        .description("Specifies whether the extension is enabled or disabled")
        .defaultValue(true);

    public static final ConfigProperty<Configuration> CONFIGURATION_CONFIG = ConfigProperty
        .ofConfig("config")
        .description("The configuration of the extension")
        .defaultValue(Configuration.empty());

    public static final ConfigProperty<Boolean> DEFAULT_CONFIG = ConfigProperty
        .ofBoolean("default")
        .description("The configuration of the extension")
        .defaultValue(false);

    public static ExtensionConfigEntry of(final @NotNull Configuration config) {
        return of(config, null);
    }

    public static ExtensionConfigEntry of(final @NotNull Configuration config, String name) {
        Objects.requireNonNull(config, "config must not be null");
        return new ExtensionConfigEntry(
            Optional.ofNullable(name).orElseGet(() -> NAME_CONFIG.get(config)),
            TYPE_CONFIG.get(config),
            PRIORITY_CONFIG.get(config),
            ENABLED_CONFIG.get(config),
            DEFAULT_CONFIG.get(config),
            CONFIGURATION_CONFIG.get(config)
        );
    }
}
