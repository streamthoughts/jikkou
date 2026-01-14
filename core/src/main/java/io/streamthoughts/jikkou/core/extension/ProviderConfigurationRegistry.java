/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for managing multiple provider configurations.
 * Allows registering and retrieving configurations for different provider instances.
 *
 * @since 0.37.0
 */
public interface ProviderConfigurationRegistry {

    /**
     * Registers a provider configuration.
     *
     * @param providerName  the name of the provider instance (e.g., "kafka-prod", "kafka-dev")
     * @param providerType  the fully qualified class name of the provider type
     * @param configuration the configuration for this provider instance
     * @param isDefault     whether this is the default provider when multiple instances exist
     */
    void registerProviderConfiguration(@NotNull String providerName,
                                       @NotNull String providerType,
                                       @NotNull Configuration configuration,
                                       boolean isDefault);

    /**
     * Gets the configuration for a specific provider instance.
     *
     * @param providerName the name of the provider instance
     * @return the configuration if found
     */
    @NotNull Optional<Configuration> getProviderConfiguration(@NotNull String providerName);

    /**
     * Gets the default configuration for a provider type.
     *
     * @param providerType the fully qualified class name of the provider type
     * @return the default configuration if found
     */
    @NotNull Optional<Configuration> getDefaultConfiguration(@NotNull String providerType);

    /**
     * Gets the configuration for a provider, selecting based on context.
     * If {@code providerName} is specified, returns that configuration.
     * Otherwise, returns the default configuration for the provider type.
     *
     * @param providerType the fully qualified class name of the provider type
     * @param providerName the selected provider name (optional)
     * @return the selected configuration
     *
     * @throws JikkouRuntimeException if no configuration is found for the given provider type and name.
     */
    @NotNull Configuration getConfiguration(@NotNull String providerType,
                                            @Nullable String providerName);

    /**
     * Gets all registered provider names.
     *
     * @return set of all provider names
     */
    @NotNull Set<String> getAllProviderNames();

    /**
     * Gets all provider names for a specific provider type.
     *
     * @param providerType the fully qualified class name of the provider type
     * @return set of provider names for this type
     */
    @NotNull Set<String> getProviderNamesByType(@NotNull String providerType);
}
