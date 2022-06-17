/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.error.JikkouException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;


/**
 * Interface for registering and supplying new {@link Extension} instances.
 */
public interface ExtensionFactory {

    /**
     * Get a new extension instance for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extension.
     * @return      the new instance of type {@link T}
     * @param <T>   the extension-type.
     */
    default <T extends Extension> T getExtension(final String type) {
        return getExtension(type, JikkouConfig.empty());
    }

    /**
     * Get a new extension instance for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extension.
     * @return      the new instance of type {@link T}
     * @param <T>   the extension-type.
     */
    <T extends Extension> T getExtension(final String type, final JikkouConfig config);

    /**
     * Get all extension instances for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extensions.
     * @return      all instances of type {@link T}.
     * @param <T>   the extension-type.
     */
    default <T extends Extension> Collection<T> getAllExtensions(final Class<T> type) {
        return getAllExtensions(type, JikkouConfig.empty());
    }

    /**
     * Get all extension instances for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extensions.
     * @return      all instances of type {@link T}.
     * @param <T>   the extension-type.
     */
    <T extends Extension> Collection<T> getAllExtensions(final Class<T> type, final JikkouConfig config);

    /**
     * Get all descriptors for registered extensions.
     *
     * @return  the list of {@link ExtensionDescriptor}.
     */
    Collection<ExtensionDescriptor> allExtensionTypes();

    /**
     * Register a new extension supplier for the given type.
     *
     * @param type      the type of the extension.
     * @param supplier  the supplier of the extension.
     */
    void register(@NotNull final Class<? extends Extension> type,
                  @NotNull final Supplier<? extends Extension> supplier);

    /**
     * Get all extension suppliers for the specified type.
     *
     * @param type  the fully qualified class name of the extensions.
     * @return      all suppliers of type {@link T}.
     * @param <T>   the extension-type.
     */
    <T extends Extension> List<Supplier<T>> getAllExtensionSupplier(@NotNull final Class<T> type);

    /**
     * Get all extension suppliers for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extensions.
     * @return      all suppliers of type {@link T}.
     * @param <T>   the extension-type.
     */
    <T extends Extension> List<Supplier<T>> getAllExtensionsSupplier(@NotNull final String type);

    /**
     * Get the extension supplier for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extensions.
     * @return      all suppliers of type {@link T}.
     * @param <T>   the extension-type.
     *
     * @throws NoUniqueExtensionException   if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException     if no extension is registered for the given type.
     */
    <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final Class<T> type);


    /**
     * Get the extension supplier for the specified type.
     *
     * @param type  the fully qualified class name or an alias of the extensions.
     * @return      all suppliers of type {@link T}.
     * @param <T>   the extension-type.
     *
     * @throws NoUniqueExtensionException   if more than one extension is registered for the given type.
     * @throws NoSuchExtensionException     if no extension is registered for the given type.
     */
    <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final String type);

    class NoSuchExtensionException extends JikkouException {

        public NoSuchExtensionException(final String message) {
            super(message);
        }
    }

    class NoUniqueExtensionException extends JikkouException {

        public NoUniqueExtensionException(final String message) {
            super(message);
        }
    }
}
