/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * ExtensionRegistry used to set extension provider.
 */
public final class ExtensionProviderAwareRegistry implements ExtensionRegistry {

    private final ExtensionRegistry delegate;
    private final Class<? extends ExtensionProvider> provider;
    private final Configuration configuration;

    /**
     * Creates a new {@link ExtensionProviderAwareRegistry} instance.
     *
     * @param delegate The ExtensionRegistry to delegate to.
     * @param provider The extension group name.
     */
    public ExtensionProviderAwareRegistry(@NotNull ExtensionRegistry delegate,
                                          @NotNull Class<? extends ExtensionProvider> provider,
                                          @NotNull Configuration configuration) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier) {
        delegate.register(type, supplier, newProviderModifier());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier,
                             ExtensionDescriptorModifier... modifiers) {
        ExtensionDescriptorModifier[] newModifiers = Arrays.copyOf(modifiers, modifiers.length + 1);
        newModifiers[newModifiers.length - 1] = newProviderModifier();
        delegate.register(type, supplier, newModifiers);
    }

    @NotNull
    private ExtensionDescriptorModifier newProviderModifier() {
        return ExtensionDescriptorModifiers.withProvider(provider, new InternalExtensionProviderSupplier(provider, configuration));
    }

    private static final class InternalExtensionProviderSupplier implements Supplier<ExtensionProvider> {

        private final Class<? extends ExtensionProvider> clazz;
        private final Configuration configuration;

        public InternalExtensionProviderSupplier(Class<? extends ExtensionProvider> clazz, Configuration configuration) {
            this.clazz = clazz;
            this.configuration = configuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ExtensionProvider get() {
            ExtensionProvider provider = Classes.newInstance(clazz, clazz.getClassLoader());
            provider.configure(configuration);
            return provider;
        }
    }
}
