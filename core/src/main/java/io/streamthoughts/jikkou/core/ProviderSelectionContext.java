/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.extension.ProviderConfigurationRegistry;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context for provider selection during extension creation.
 * This context flows through the extension system to enable provider-specific configuration selection.
 *
 * @since 0.37.0
 */
@InterfaceStability.Evolving
public record ProviderSelectionContext(
    @Nullable String selectedProvider,
    @NotNull ProviderConfigurationRegistry registry) {

    /**
     * Creates an empty provider selection context with no specific provider selected.
     *
     * @param registry the provider configuration registry
     * @return an empty ProviderSelectionContext
     */
    public static ProviderSelectionContext empty(@NotNull ProviderConfigurationRegistry registry) {
        return new ProviderSelectionContext(null, registry);
    }

    /**
     * Creates a provider selection context with a specific provider selected.
     * Validates that the provider exists in the registry.
     *
     * @param providerName the selected provider name
     * @param registry     the provider configuration registry
     * @return a ProviderSelectionContext with the selected provider
     * @throws IllegalArgumentException if the provider doesn't exist
     */
    public static ProviderSelectionContext of(String providerName,
                                              @NotNull ProviderConfigurationRegistry registry) {
        if (Strings.isNullOrEmpty(providerName)) {
            return empty(registry);
        }

        // Validate provider exists
        Set<String> availableProviders = registry.getAllProviderNames();
        if (!availableProviders.contains(providerName)) {
            throw new IllegalArgumentException(String.format(
                "Provider '%s' not found. Available providers: %s.",
                providerName,
                availableProviders
            ));
        }

        return new ProviderSelectionContext(providerName, registry);
    }

    /**
     * Checks if a specific provider is selected.
     *
     * @return true if a provider is selected, false otherwise
     */
    public boolean hasSelectedProvider() {
        return !Strings.isNullOrEmpty(selectedProvider);
    }
}
