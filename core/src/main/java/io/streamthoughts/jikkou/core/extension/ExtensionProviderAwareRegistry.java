/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.core.ProviderSelectionContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ExtensionRegistry used to set extension provider.
 */
public final class ExtensionProviderAwareRegistry implements ExtensionRegistry {

    private final ExtensionRegistry delegate;
    private final Class<? extends ExtensionProvider> provider;
    private final Configuration configuration;
    private final ProviderConfigurationRegistry providerConfigurationRegistry;
    private final ExtensionDescriptorModifier[] extraModifiers;

    /**
     * Creates a new {@link ExtensionProviderAwareRegistry} instance with provider configuration registry.
     *
     * @param delegate The ExtensionRegistry to delegate to.
     * @param provider The extension group name.
     * @param configuration The default configuration.
     * @param providerConfigurationRegistry The provider configuration registry (optional).
     */
    public ExtensionProviderAwareRegistry(@NotNull ExtensionRegistry delegate,
                                          @NotNull Class<? extends ExtensionProvider> provider,
                                          @NotNull Configuration configuration,
                                          @Nullable ProviderConfigurationRegistry providerConfigurationRegistry) {
        this(delegate, provider, configuration, providerConfigurationRegistry, new ExtensionDescriptorModifier[0]);
    }

    /**
     * Creates a new {@link ExtensionProviderAwareRegistry} instance with provider configuration registry
     * and additional modifiers.
     *
     * @param delegate The ExtensionRegistry to delegate to.
     * @param provider The extension group name.
     * @param configuration The default configuration.
     * @param providerConfigurationRegistry The provider configuration registry (optional).
     * @param extraModifiers Additional modifiers to apply to each registered extension descriptor.
     */
    public ExtensionProviderAwareRegistry(@NotNull ExtensionRegistry delegate,
                                          @NotNull Class<? extends ExtensionProvider> provider,
                                          @NotNull Configuration configuration,
                                          @Nullable ProviderConfigurationRegistry providerConfigurationRegistry,
                                          @NotNull ExtensionDescriptorModifier... extraModifiers) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration cannot be null");
        this.providerConfigurationRegistry = providerConfigurationRegistry;
        this.extraModifiers = Objects.requireNonNull(extraModifiers, "extraModifiers cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier) {
        delegate.register(type, supplier, allModifiers());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier,
                             ExtensionDescriptorModifier... modifiers) {
        ExtensionDescriptorModifier[] combined = allModifiers(modifiers);
        delegate.register(type, supplier, combined);
    }

    @NotNull
    private ExtensionDescriptorModifier[] allModifiers(ExtensionDescriptorModifier... modifiers) {
        ExtensionDescriptorModifier[] result = Arrays.copyOf(modifiers, modifiers.length + 1 + extraModifiers.length);
        result[modifiers.length] = newProviderModifier();
        System.arraycopy(extraModifiers, 0, result, modifiers.length + 1, extraModifiers.length);
        return result;
    }

    @NotNull
    private ExtensionDescriptorModifier newProviderModifier() {
        return ExtensionDescriptorModifiers.withProvider(
            provider, 
            new InternalExtensionProviderSupplier(provider, configuration, providerConfigurationRegistry)
        );
    }

    /**
     * Internal supplier for creating ExtensionProvider instances with context-aware configuration selection.
     */
    private static final class InternalExtensionProviderSupplier implements ProviderSupplier {

        private final Class<? extends ExtensionProvider> clazz;
        private final Configuration defaultConfiguration;
        private final ProviderConfigurationRegistry providerConfigurationRegistry;

        public InternalExtensionProviderSupplier(Class<? extends ExtensionProvider> clazz, 
                                                Configuration defaultConfiguration,
                                                @Nullable ProviderConfigurationRegistry providerConfigurationRegistry) {
            this.clazz = clazz;
            this.defaultConfiguration = defaultConfiguration;
            this.providerConfigurationRegistry = providerConfigurationRegistry;
        }

        /**
         * Gets a configured ExtensionProvider instance, optionally using a ProviderSelectionContext
         * to select a specific provider configuration.
         *
         * @param providerContext the provider selection context (optional)
         * @return a configured ExtensionProvider instance
         */
        @Override
        public ExtensionProvider get(@Nullable ProviderSelectionContext providerContext) {
            Configuration config = selectConfiguration(providerContext);
            ExtensionProvider provider = Classes.newInstance(clazz, clazz.getClassLoader());
            provider.configure(config);
            return provider;
        }

        /**
         * Selects the appropriate configuration based on the provider selection context.
         *
         * @param providerContext the provider selection context (optional)
         * @return the selected configuration
         */
        private Configuration selectConfiguration(@Nullable ProviderSelectionContext providerContext) {
            // If no provider configuration registry is available, use default
            if (providerConfigurationRegistry == null) {
                return defaultConfiguration;
            }

            // Get provider type name
            String providerType = clazz.getName();

            // Try to get selected provider from context
            String selectedProvider = null;
            if (providerContext != null && providerContext.hasSelectedProvider()) {
                selectedProvider = providerContext.selectedProvider();
            }

            // Get configuration from registry
            Configuration config = providerConfigurationRegistry.getConfiguration(providerType, selectedProvider);

            // Fallback to default configuration if not found in registry
            return config.withFallback(defaultConfiguration);
        }
    }
}
