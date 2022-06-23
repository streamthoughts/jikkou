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

import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.api.model.ResourceList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceLoader.class);

    private ResourceReaderOptions options = new ResourceReaderOptions();

    private final List<String> valuesFiles = new LinkedList<>();

    /**
     * Helper method to create a default {@link ResourceLoader} for reading specification in YAML files.
     *
     * @return a new {@link ResourceLoader}.
     */
    public static ResourceLoader create() {
        return new ResourceLoader();
    }

    public ResourceLoader valuesFile(final @NotNull String valuesFile) {
        this.valuesFiles.add(valuesFile);
        return this;
    }
    public ResourceLoader valuesFiles(final @NotNull List<String> valuesFiles) {
        this.valuesFiles.addAll(valuesFiles);
        return this;
    }

    public ResourceLoader options(@NotNull final ResourceReaderOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Loads specifications for Kafka resources from the classpath resource.
     *
     * @param resourceName name of the classpath resource to load.
     * @return a new {@link ResourceList}.
     */
    public ResourceList loadFromClasspath(@NotNull final String resourceName) {
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
     * @return a new {@link ResourceList}.
     */
    public ResourceList load(@NotNull final InputStream file) {

        final ResourceReaderOptions newOptions = getOptionsWithAllValues();

        var factory = ResourceReaderFactory.INSTANCE;
        return new ResourceList(factory.create(file).readAllResources(newOptions));
    }

    /**
     * Loads specifications for Kafka resources from YAML files, directories or URLs.
     *
     * @param locations locations from which to load specifications.
     * @return a list of {@link ResourceList}.
     */
    public ResourceList load(final @NotNull List<String> locations) {
        if (locations.isEmpty()) {
            throw new JikkouException("No resource specification file loaded");
        }

        final ResourceReaderOptions newOptions = getOptionsWithAllValues();

        var factory = ResourceReaderFactory.INSTANCE;
        return new ResourceList(locations.stream()
                .map(location -> factory.create(URI.create(location)))
                .flatMap(reader -> reader.readAllResources(newOptions).stream())
                .toList()
        );
    }

    private ResourceReaderOptions getOptionsWithAllValues() {
        return new ResourceReaderOptions()
                .withValues(loadAllValuesFiles())
                .withValues(options.values())
                .withLabels(options.labels())
                .withPattern(options.pattern());
    }

    private @NotNull NamedValue.Set loadAllValuesFiles() {
        return valuesFiles
                .parallelStream()
                .flatMap(this::loadValuesFromFile)
                .sorted(Comparator.comparing(ValuesFile::file))
                .map(ValuesFile::values)
                .reduce(NamedValue.emptySet(), NamedValue.Set::with);
    }

    private @NotNull Stream<ValuesFile> loadValuesFromFile(final String filePath) {
        try {
            Path path = Path.of(filePath);
            if (Files.size(path) > 0) {
                try (InputStream src = Files.newInputStream(path)) {
                    Map<String, Object> values = Jackson.YAML_OBJECT_MAPPER.readValue(src, Map.class);
                    return Stream.of(new ValuesFile(filePath, NamedValue.setOf(values)));
                }
            } else {
                LOG.debug("Ignore values from '{}'. File is empty.", filePath);
                return Stream.empty();
            }
        } catch (IOException e) {
            throw new JikkouException(
                    String.format("Failed to load values-file '%s': %s", filePath, e.getLocalizedMessage())
            );
        }
    }

    private record ValuesFile(String file, NamedValue.Set values) { }

}
