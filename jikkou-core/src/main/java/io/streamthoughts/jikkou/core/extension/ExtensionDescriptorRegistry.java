/*
 * Copyright 2019-2020 The original authors
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
import io.streamthoughts.jikkou.core.extension.exceptions.NoUniqueExtensionException;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to register extension descriptors.
 */
public interface ExtensionDescriptorRegistry extends ExtensionRegistry {

    /**
     * Gets all the registered descriptors.
     *
     * @return the collection of {@link ExtensionDescriptor}.
     */
    List<ExtensionDescriptor<?>> getAllDescriptors();

    /**
     * Finds all {@link ExtensionDescriptor} registered for the specified type.
     *
     * @param type the extension class.
     * @param <T>  the extension type.
     * @return the collection of {@link ExtensionDescriptor}.
     */
    <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull final Class<T> type);

    /**
     * Finds all {@link ExtensionDescriptor} registered for the specified type.
     *
     * @param type the extension class.
     * @param <T>  the extension type.
     * @return the collection of {@link ExtensionDescriptor}.
     */
    <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull final Class<T> type,
                                                               @Nullable final Qualifier<T> qualifier);

    /**
     * Finds all {@link ExtensionDescriptor} registered for the specified alias.
     *
     * @param alias the fully qualified class name or an alias of the extension.
     * @param <T>   the extension type.
     * @return the collection of {@link ExtensionDescriptor}.
     */
    <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull final String alias);

    /**
     * Finds all {@link ExtensionDescriptor} registered for the specified alias.
     *
     * @param alias the fully qualified class name or an alias of the extension.
     * @param <T>   the extension type.
     * @return the collection of {@link ExtensionDescriptor}.
     */
    <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull final String alias,
                                                               @Nullable final Qualifier<T> qualifier);

    /**
     * Finds a {@link ExtensionDescriptor} for the specified type.
     *
     * @param alias the fully qualified class name or an alias of the extension.
     * @param <T>   the extension type.
     * @return the optional {@link ExtensionDescriptor} instance.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     */
    <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull final String alias);

    /**
     * Finds a {@link ExtensionDescriptor} for the specified type and options.
     *
     * @param alias     the fully qualified class name or an alias of the extension.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the extension type.
     * @return the optional {@link ExtensionDescriptor} instance.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     */
    <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull final String alias,
                                                               @Nullable final Qualifier<T> qualifier);

    /**
     * Finds a {@link ExtensionDescriptor} for the specified type.
     *
     * @param type the extension class.
     * @param <T>  the extension type.
     * @return the optional {@link ExtensionDescriptor} instance.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     */
    <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull final Class<T> type);

    /**
     * Finds a {@link ExtensionDescriptor} for the specified type and options.
     *
     * @param type      the fully qualified class name or an alias of the extension.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the extension type.
     * @return the optional {@link ExtensionDescriptor} instance.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     */
    <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull final Class<T> type,
                                                               @Nullable final Qualifier<T> qualifier);


    <T> ExtensionSupplier<T> getExtensionSupplier(ExtensionDescriptor<T> descriptor);

    /**
     * Registers the specified {@link ExtensionDescriptor} to this {@link ExtensionDescriptorRegistry}.
     *
     * @param descriptor the {@link ExtensionDescriptor} instance to be registered.
     * @param <T>        the extension type.
     * @throws ConflictingExtensionDefinitionException if an extension is already register for that descriptor.
     */
    <T> void registerDescriptor(@NotNull final ExtensionDescriptor<T> descriptor);

    /**
     * Duplicates this registry.
     *
     * @return a new {@link ExtensionDescriptorRegistry} instance.
     */
    ExtensionDescriptorRegistry duplicate();
}
