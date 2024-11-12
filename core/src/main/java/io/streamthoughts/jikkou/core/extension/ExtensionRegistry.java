/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to register extensions.
 */
public interface ExtensionRegistry {


    /**
     * Register an extension supplier for the specified extension type.
     *
     * @param type     the class of the extension.
     * @param <T>      type of the extension.
     * @throws NullPointerException                    if the given type of supplier is {@code null}.
     * @throws ConflictingExtensionDefinitionException if an extension is already register for that type.
     */
    default <T> void register(@NotNull Class<T> type) {
        register(type, () -> Classes.newInstance(type));
    }

    /**
     * Register an extension supplier for the specified extension type.
     *
     * @param type     the class of the extension.
     * @param supplier the supplier used to create a new instance of {@code T}.
     * @param <T>      type of the extension.
     * @throws NullPointerException                    if the given type of supplier is {@code null}.
     * @throws ConflictingExtensionDefinitionException if an extension is already register for that type.
     */
    <T> void register(@NotNull Class<T> type,
                      @NotNull Supplier<T> supplier);

    /**
     * Registers the component supplier for the specified type and name.
     *
     * @param type      the class of the extension.
     * @param supplier  the supplier used to create a new instance of {@code T}.
     * @param modifiers the component descriptor modifiers.
     * @param <T>       the component-type.
     * @throws NullPointerException                    if the given type of supplier is {@code null}.
     * @throws ConflictingExtensionDefinitionException if an extension is already register for that type.
     */
    <T> void register(@NotNull Class<T> type,
                      @NotNull Supplier<T> supplier,
                      ExtensionDescriptorModifier... modifiers);
}
