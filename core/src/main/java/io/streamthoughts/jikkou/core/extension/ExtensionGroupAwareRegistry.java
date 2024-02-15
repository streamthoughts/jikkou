/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * ExtensionRegistry used to set extension group.
 */
public final class ExtensionGroupAwareRegistry implements ExtensionRegistry {

    private final ExtensionRegistry delegate;
    private final String extensionGroup;

    /**
     * Creates a new {@link ExtensionGroupAwareRegistry} instance.
     * @param delegate          The ExtensionRegistry to delegate to.
     * @param extensionGroup    The extension group name.
     */
    public ExtensionGroupAwareRegistry(@NotNull ExtensionRegistry delegate,
                                        @NotNull String extensionGroup) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.extensionGroup = Objects.requireNonNull(extensionGroup, "extensionGroup cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier) {
        delegate.register(type, supplier, ExtensionDescriptorModifiers.withProvider(extensionGroup));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier,
                             ExtensionDescriptorModifier... modifiers) {
        ExtensionDescriptorModifier[] newModifiers = Arrays.copyOf(modifiers, modifiers.length + 1);
        newModifiers[newModifiers.length - 1] = ExtensionDescriptorModifiers.withProvider(extensionGroup);
        delegate.register(type, supplier, newModifiers);
    }
}
