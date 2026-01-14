/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ProviderConfigurationRegistry}.
 *
 * @since 0.37.0
 */
public class DefaultProviderConfigurationRegistry implements ProviderConfigurationRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProviderConfigurationRegistry.class);

    private final Map<String, ProviderEntry> providerByName = new ConcurrentHashMap<>();
    private final Map<String, List<ProviderEntry>> providerByType = new ConcurrentHashMap<>();
    private final Map<String, String> defaultProviderByType = new ConcurrentHashMap<>();

    @Override
    public void registerProviderConfiguration(@NotNull String providerName,
                                              @NotNull String providerType,
                                              @NotNull Configuration configuration,
                                              boolean isDefault) {
        LOG.debug("Registering provider configuration: name={}, type={}, isDefault={}", providerName, providerType, isDefault);

        if (providerByName.containsKey(providerName)) {
            throw new IllegalArgumentException("Configuration is already registered for provider name: " + providerName);
        }

        providerByName.put(providerName, new ProviderEntry(providerType, configuration));
        providerByType.computeIfAbsent(providerType, unsued -> new ArrayList<>()).add(new ProviderEntry(providerType, configuration));

        if (isDefault) {
            String existing = defaultProviderByType.put(providerType, providerName);
            if (existing != null && !existing.equals(providerName)) {
                LOG.warn("Replacing default provider for type '{}': old='{}', new='{}'",
                    providerType, existing, providerName);
            }
        }
    }

    @Override
    public @NotNull Optional<Configuration> getProviderConfiguration(@NotNull String providerName) {
        ProviderEntry entry = providerByName.get(providerName);
        if (entry == null) {
            LOG.debug("Provider configuration not found: name={}", providerName);
            return Optional.empty();
        }
        return Optional.of(entry.configuration());
    }

    @Override
    public @NotNull Optional<Configuration> getDefaultConfiguration(@NotNull String providerType) {
        String defaultProvider = defaultProviderByType.get(providerType);
        if (defaultProvider == null) {
            List<ProviderEntry> providers = providerByType.get(providerType);
            if (providers != null && providers.size() == 1) {
                return Optional.of(providers.getFirst().configuration());
            }
            LOG.debug("No default provider configured for type: {}", providerType);
            return Optional.empty();
        }
        return getProviderConfiguration(defaultProvider);
    }

    @Override
    public @NotNull Configuration getConfiguration(@NotNull String providerType,
                                                   @Nullable String providerName) {
        Objects.requireNonNull(providerType, "providerType cannot be null");
        LOG.debug("Getting provider configuration: type={}, name={}", providerType, providerName);
        if (Strings.isNullOrEmpty(providerName)) {
            return getDefaultConfiguration(providerType)
                .or(() -> getProviderNamesByType(providerType).isEmpty() ? Optional.of(Configuration.empty()) : Optional.empty())
                .orElseThrow(() -> new JikkouRuntimeException("No default configuration defined, and multiple configurations found for provider type: '%s'".formatted(providerType)));
        }
        return getProviderConfiguration(providerName)
            .orElseThrow(() -> new JikkouRuntimeException("No provider configured for name: '%s'".formatted(providerName)));
    }

    @Override
    public @NotNull Set<String> getAllProviderNames() {
        return providerByName.keySet();
    }

    @Override
    public @NotNull Set<String> getProviderNamesByType(@NotNull String providerType) {
        return providerByName.entrySet().stream()
            .filter(entry -> entry.getValue().providerType().equals(providerType))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * Internal record to store provider configuration entries.
     */
    private record ProviderEntry(String providerType, Configuration configuration) {
    }
}
