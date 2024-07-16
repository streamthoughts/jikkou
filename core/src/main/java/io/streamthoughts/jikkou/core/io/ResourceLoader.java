/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.ResourceList;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ResourceLoader {

    private final ResourceReaderOptions options;
    private final ResourceReaderFactory factory;

    /**
     * Creates a new {@link ResourceLoader} instance.
     *
     * @param factory   the ResourceReader factory.
     */
    public ResourceLoader(final @NotNull ResourceReaderFactory factory) {
        this(factory, new ResourceReaderOptions());
    }

    /**
     * Creates a new {@link ResourceLoader} instance.
     *
     * @param factory   the ResourceReader factory.
     * @param options   the ResourceReader options.
     */
    public ResourceLoader(final @NotNull ResourceReaderFactory factory,
                          final @NotNull ResourceReaderOptions options) {
        this.options = options;
        this.factory = factory;
    }

    /**
     * Loads resource definitions from the given classpath resource.
     *
     * @param resourceName name of the classpath resource to load.
     * @return a new {@link HasItems}.
     */
    public HasItems loadFromClasspath(@NotNull final String resourceName) {
        return Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(resourceName))
                .map(this::load)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Failed to load resource from classpath: '%s'", resourceName))
                );
    }

    /**
     * Loads resource definitions from the given {@code InputStream}.
     *
     * @param file the input stream.
     * @return a new {@link HasItems}.
     */
    public HasItems load(@NotNull final InputStream file) {
        return ResourceList.of(factory.create(file).readAllResources(options));
    }

    /**
     * Loads resource definitions from the given locations, e.g., files, directories, or URLs.
     *
     * @param locations locations from which to resource definitions.
     * @return a list of {@link HasItems}.
     */
    public HasItems load(final @NotNull List<String> locations) {
        if (locations.isEmpty()) {
            throw new JikkouRuntimeException("No resource definition file loaded");
        }

        return ResourceList.of(locations.stream()
                .map(location -> factory.create(URI.create(location)))
                .flatMap(reader -> reader.readAllResources(options).stream())
                .toList()
        );
    }
}
