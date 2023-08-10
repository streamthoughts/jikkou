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
package io.streamthoughts.jikkou.api.io;

import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ResourceLoader {

    private ResourceReaderOptions options = new ResourceReaderOptions();

    private ResourceReaderFactory factory = new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER);

    /**
     * Helper method to create a default {@link ResourceLoader} for reading specification in YAML files.
     *
     * @return a new {@link ResourceLoader}.
     */
    public static ResourceLoader create() {
        return new ResourceLoader();
    }

    public ResourceLoader withResourceReaderOptions(@NotNull final ResourceReaderOptions options) {
        this.options = options;
        return this;
    }

    public ResourceLoader withResourceReaderFactory(@NotNull final ResourceReaderFactory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Loads specifications for Kafka resources from the classpath resource.
     *
     * @param resourceName name of the classpath resource to load.
     * @return a new {@link GenericResourceListObject}.
     */
    public HasItems loadFromClasspath(@NotNull final String resourceName) {
        return Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(resourceName))
                .map(this::load)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Failed to load resource from classpath: '%s'", resourceName))
                );
    }

    /**
     * Loads specifications for Kafka resources from the given {@code InputStream}.
     *
     * @param file the input stream.
     * @return a new {@link GenericResourceListObject}.
     */
    public HasItems load(@NotNull final InputStream file) {

        return new GenericResourceListObject(factory.create(file).readAllResources(options));
    }

    /**
     * Loads specifications for Kafka resources from YAML files, directories or URLs.
     *
     * @param locations locations from which to load specifications.
     * @return a list of {@link GenericResourceListObject}.
     */
    public HasItems load(final @NotNull List<String> locations) {
        if (locations.isEmpty()) {
            throw new JikkouRuntimeException("No resource specification file loaded");
        }

        return new GenericResourceListObject(locations.stream()
                .map(location -> factory.create(URI.create(location)))
                .flatMap(reader -> reader.readAllResources(options).stream())
                .toList()
        );
    }
}
