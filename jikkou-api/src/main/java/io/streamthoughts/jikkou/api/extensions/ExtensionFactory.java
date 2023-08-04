/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;


/**
 * Interface for registering and supplying new {@link Extension} instances.
 */
@InterfaceStability.Evolving
public interface ExtensionFactory {

    /**
     * Get a new extension instance for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extension.
     * @param <T>  the extension-type.
     * @return the new instance of type {@link T}
     */
    default <T extends Extension> T getExtension(final String type) {
        return getExtension(type, Configuration.empty());
    }

    /**
     * Get a new extension instance for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extension.
     * @param <T>  the extension-type.
     * @return the new instance of type {@link T}
     */
    <T extends Extension> T getExtension(final String type, final Configuration config);

    /**
     * Get a new extension instance for the specified type.
     *
     * @param type the class name or an alias of the extension.
     * @param <T>  the extension-type.
     * @return the new instance of type {@link T}
     */
    <T extends Extension> T getExtension(final Class<T> type,
                                         final Configuration config);

    /**
     * Get all extension instances for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extensions.
     * @param <T>  the extension-type.
     * @return all instances of type {@link T}.
     */
    default <T extends Extension> Collection<T> getAllExtensions(final Class<T> type) {
        return getAllExtensions(type, Configuration.empty(), descriptor -> true);
    }

    /**
     * Get all extension instances for the specified type.
     *
     * @param type      the fully qualified class name or an alias of the extensions.
     * @param <T>       the extension-type.
     * @param predicate the predicate to be used for filtering extensions.
     * @return all instances of type {@link T}.
     */
    default <T extends Extension> Collection<T> getAllExtensions(final Class<T> type,
                                                                 final Predicate<ExtensionDescriptor<T>> predicate) {
        return getAllExtensions(type, Configuration.empty(), predicate);
    }

    /**
     * Get all extension instances for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extensions.
     * @param <T>  the extension-type.
     * @return all instances of type {@link T}.
     */
    default <T extends Extension> List<T> getAllExtensions(final Class<T> type,
                                                           final Configuration config) {
        return getAllExtensions(type, config, descriptor -> true);
    }

    /**
     * Get all extension instances for the specified type.
     *
     * @param type      the fully qualified class name or an alias of the extensions.
     * @param <T>       the extension-type.
     * @param predicate the predicate to be used for filtering extensions.
     * @return all instances of type {@link T}.
     */
    <T extends Extension> List<T> getAllExtensions(final Class<T> type,
                                                   final Configuration config,
                                                   final Predicate<ExtensionDescriptor<T>> predicate);

    /**
     * Get all descriptors for registered extensions.
     *
     * @return the list of {@link ExtensionDescriptor}.
     */
    Collection<ExtensionDescriptor<?>> allExtensionTypes();

    /**
     * Get all descriptors for registered extensions for the specified type.
     *
     * @return the list of {@link ExtensionDescriptor}.
     */
    <T extends Extension> Collection<ExtensionDescriptor<T>> getAllDescriptorsForType(@NotNull final Class<T> type);

    /**
     * Register a new extension supplier for the given type.
     *
     * @param type     the type of the extension.
     * @param supplier the supplier of the extension.
     */
    <T extends Extension> void register(@NotNull final Class<T> type,
                                        @NotNull final Supplier<T> supplier);

    /**
     * Get all extension suppliers for the specified type.
     *
     * @param type the fully qualified class name of the extensions.
     * @param <T>  the extension-type.
     * @return all suppliers of type {@link T}.
     */
    <T extends Extension> List<Supplier<T>> getAllExtensionSupplier(@NotNull final Class<T> type);

    /**
     * Get all extension suppliers for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extensions.
     * @param <T>  the extension-type.
     * @return all suppliers of type {@link T}.
     */
    <T extends Extension> List<Supplier<T>> getAllExtensionsSupplier(@NotNull final String type);

    /**
     * Get the extension supplier for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extensions.
     * @param <T>  the extension-type.
     * @return all suppliers of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given type.
     */
    <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final Class<T> type);


    /**
     * Get the extension supplier for the specified type.
     *
     * @param type the fully qualified class name or an alias of the extensions.
     * @param <T>  the extension-type.
     * @return all suppliers of type {@link T}.
     * @throws NoUniqueExtensionException if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException   if no extension is registered for the given type.
     */
    <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final String type);

    class NoSuchExtensionException extends JikkouRuntimeException {

        public NoSuchExtensionException(final String message) {
            super(message);
        }
    }

    class NoUniqueExtensionException extends JikkouRuntimeException {

        public NoUniqueExtensionException(final String message) {
            super(message);
        }
    }
}
