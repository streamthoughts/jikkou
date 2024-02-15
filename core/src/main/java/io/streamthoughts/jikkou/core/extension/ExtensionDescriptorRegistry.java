/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
     * Finds all {@link ExtensionDescriptor} registered for the specified qualifier.
     *
     * @param qualifier The qualifier.
     * @return the collection of {@link ExtensionDescriptor}.
     */
    List<ExtensionDescriptor<?>> findAllDescriptors(@NotNull final Qualifier<?> qualifier);

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

    /**
     * Gets the extension supplier for the specified descriptor.
     *
     * @param descriptor The description.
     * @param <T>        The type of the extension.
     * @return The ExtensionSupplier.
     */
    <T> ExtensionSupplier<T> getExtensionSupplier(@NotNull final ExtensionDescriptor<T> descriptor);

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
