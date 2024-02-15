/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.extension.exceptions.NoUniqueExtensionException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for registering and supplying new {@link Extension} instances.
 */
public interface ExtensionFactory extends ExtensionDescriptorRegistry {

    /**
     * Checks if at least one extension is registered for the given type.
     *
     * @param type the fully qualified class name or an alias of the extension.
     * @return {@code true} if a provider exist, {@code false} otherwise.
     */
    boolean containsExtension(final String type);

    /**
     * Checks if at least one extension is registered for the given type and qualifier.
     *
     * @param type      the extension type.
     * @param qualifier the options to qualify the extension.
     * @return {@code true} if a provider exist, {@code false} otherwise.
     */
    <T> boolean containsExtension(@NotNull final String type,
                                  @Nullable final Qualifier<T> qualifier);

    /**
     * Checks if at least one extension is registered for the given type.
     *
     * @param type the extension type.
     * @return {@code true} if a provider exist, {@code false} otherwise.
     */
    <T> boolean containsExtension(@NotNull final Class<T> type);

    /**
     * Checks if at least one extension is registered for the given type and qualifier.
     *
     * @param type      the extension type.
     * @param qualifier the options to qualify the extension.
     * @return {@code true} if a provider exist, {@code false} otherwise.
     */
    <T> boolean containsExtension(@NotNull final Class<T> type,
                                  @Nullable final Qualifier<T> qualifier);

    /**
     * Gets a new instance for the specified type.
     *
     * @param type the extension class.
     * @param <T>  the type of the extension.
     * @return the instance of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given type.
     */
    <T> T getExtension(@NotNull final Class<T> type);

    /**
     * Gets a new extension instance for the specified type.
     *
     * @param type      the extension class.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the type of the extension.
     * @return the instance of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given type.
     */
    <T> T getExtension(@NotNull final Class<T> type,
                       @Nullable final Qualifier<T> qualifier);


    /**
     * Finds a new extension instance for the specified type.
     *
     * @param type      the extension class.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the type of the extension.
     * @return an optional instance of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     */
    <T> Optional<T> findExtension(@NotNull final Class<T> type,
                                  @Nullable final Qualifier<T> qualifier);

    /**
     * Gets a new instance for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extension.
     * @param <T>  the type of the extension.
     * @return the instance of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given class or alias..
     */
    <T> T getExtension(@NotNull final String type);

    /**
     * Gets a new instance for the specified type and qualifier.
     *
     * @param type      the fully qualified class name or an alias of the extension.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the type of the extension.
     * @return the instance of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given class or alias.
     */
    <T> T getExtension(@NotNull final String type,
                       @Nullable final Qualifier<T> qualifier);

    /**
     * Gets all instances for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extension.
     * @param <T>  the type of the extension.
     * @return all instances of type {@link T}.
     */
    <T> Collection<T> getAllExtensions(@NotNull final String type);

    /**
     * Gets all instances for the specified type and qualifier.
     *
     * @param type      the fully qualified class name or an alias of the extension.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the type of the extension.
     * @return all instances of type {@link T}.
     */
    <T> Collection<T> getAllExtensions(@NotNull final String type,
                                       @Nullable final Qualifier<T> qualifier);

    /**
     * Gets all instances for the specified type.
     *
     * @param type the extension class.
     * @param <T>  the type of the extension.
     * @return all instances of type {@link T}.
     */
    <T> List<T> getAllExtensions(@NotNull final Class<T> type);

    /**
     * Gets all instances, which may be shared or independent, for the specified type.
     *
     * @param type      the extension class.
     * @param qualifier the options used to qualify the extension.
     * @param <T>       the type of the extension.
     * @return all instances of type {@link T}.
     */
    <T> List<T> getAllExtensions(@NotNull final Class<T> type,
                                 @Nullable final Qualifier<T> qualifier);


    /**
     * Duplicates this factory.
     *
     * @return a new {@link ExtensionFactory} instance.
     */
    ExtensionFactory duplicate();

}
