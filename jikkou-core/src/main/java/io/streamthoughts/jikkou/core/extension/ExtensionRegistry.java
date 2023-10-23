/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.extension;

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
