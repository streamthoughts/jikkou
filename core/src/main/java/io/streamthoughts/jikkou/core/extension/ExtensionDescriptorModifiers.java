/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Standard {@link ExtensionDescriptorModifier}.
 */
public final class ExtensionDescriptorModifiers {

    /**
     * Gets a modifier implementation that will set the priority of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withPriority(@NotNull final Integer priority) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>builder(descriptor)
                    .priority(priority)
                    .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will set the configuration of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withConfiguration(@NotNull final Configuration configuration) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>builder(descriptor)
                    .configuration(configuration)
                    .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will set the provider of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withProvider(@NotNull final Class<? extends ExtensionProvider> provider,
                                                           @NotNull final Supplier<? extends ExtensionProvider> supplier) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>builder(descriptor)
                        .provider(provider, supplier)
                        .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will set the name of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withName(@NotNull final String name) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>builder(descriptor)
                        .name(name)
                        .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will enable the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier enabled(final boolean enabled) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>builder(descriptor)
                        .isEnabled(enabled)
                        .build();
            }
        };
    }
}
