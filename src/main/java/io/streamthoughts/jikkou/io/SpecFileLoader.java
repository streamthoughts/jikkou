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
package io.streamthoughts.jikkou.io;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.MetaObject;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.internal.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SpecFileLoader {

    private static final Logger LOG = LoggerFactory.getLogger(SpecFileLoader.class);

    private String pattern;

    private final SpecFileReader reader;

    private Map<String, Object> values = new HashMap<>();
    private Map<String, Object> labels = new HashMap<>();
    private List<String> valuesFiles = new LinkedList<>();

    /**
     * Helper method to create a default {@link SpecFileLoader} for reading specification in YAML files.
     *
     * @return a new {@link SpecFileLoader}.
     */
    public static SpecFileLoader newForYaml() {
        return new SpecFileLoader(new YAMLSpecReader()).withPattern("**/*.{yaml,yml}");
    }

    /**
     * Creates a new {@link SpecFileLoader} instance.
     *
     * @param reader the reader to be used for reading the spec files.
     */
    private SpecFileLoader(@NotNull final SpecFileReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader should not be null");
    }

    public SpecFileLoader withPattern(@NotNull final String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern should not be null");
        return this;
    }

    public SpecFileLoader withLabels(@NotNull final Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    public SpecFileLoader withValues(@NotNull final Map<String, Object> values) {
        this.values = values;
        return this;
    }

    public SpecFileLoader withValuesFiles(@NotNull final List<String> values) {
        this.valuesFiles = values;
        return this;
    }

    /**
     * Loads specifications for Kafka resources from the classpath resource.
     *
     * @param resourceName  name of the classpath resource to load.
     * @return              a new {@link V1SpecFile}.
     */
    public V1SpecFile loadFromClasspath(@NotNull final String resourceName) {
        return Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(resourceName))
                .map(this::load)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Failed to load resource from classpath: '%s'", resourceName))
                );
    }

    /**
     * Loads specifications for Kafka resources from the given {@code InputStream}.
     *
     * @param file  the input stream.
     * @return      a new {@link V1SpecFile}.
     */
    public V1SpecFile load(@NotNull final InputStream file) {
        Map<String, Object> templatingValues = JikkouParams.TEMPLATE_VALUES_CONFIG.get(JikkouConfig.get());

        for (String valuesFile : valuesFiles) {
            try {
                Path path = Path.of(valuesFile);
                if (Files.size(path) > 0) {
                    Map<String, Object> values = Jackson.YAML_OBJECT_MAPPER.readValue(Files.newInputStream(path), Map.class);
                    if (values != null) {
                        templatingValues.putAll(values);
                    }
                } else {
                    LOG.debug("Ignore values from '{}'. File is empty.", valuesFile);
                }
            } catch (IOException e) {
                throw new JikkouException(
                    String.format("Failed to load values-file '%s': %s", valuesFile, e.getLocalizedMessage())
                );
            }
        }
        templatingValues.putAll(values);
        return reader.read(file, templatingValues, labels);
    }

    /**
     * Loads specifications for Kafka resources from YAML files, directories or URLs.
     *
     * @param locations locations from which to load specifications.
     * @return          a list of {@link V1SpecFile}.
     */
    public List<V1SpecFile> load(final @NotNull List<String> locations) {
        if (locations.isEmpty()) {
            throw new JikkouException("No resource specification file loaded");
        }

        List<V1SpecFile> loaded = locations.stream()
                .flatMap(file -> {
                    try {
                        if (file.startsWith("http://") || file.startsWith("https://")) {
                            InputStream is = new URL(file).openStream();
                            V1SpecFile spec = load(is);
                            spec.metadata().setAnnotation(MetaObject.ANNOT_RESOURCE, file);
                            return Stream.of(spec);
                        }
                        return load(Path.of(file));
                    } catch (Exception e) {
                        throw new JikkouException(
                                "Failed to read specification from '" + file + "'. " +
                                        "Error : " + e.getMessage()
                        );
                    }
                })
                .toList();
        if (loaded.isEmpty()) {
            throw new JikkouException("No resource specification file loaded");
        }

        return loaded;
    }

    @NotNull
    private Stream<V1SpecFile> load(final Path path) {
        List<Path> matching = Files.isDirectory(path) ?
                IOUtils.findMatching(path, pattern) :
                List.of(path);

        return matching.parallelStream()
                .map(p -> {
                    LOG.info("Loading specification file from '{}'", p);
                    try {
                        InputStream is = Files.newInputStream(p);
                        V1SpecFile spec = load(is);
                        spec.metadata().setAnnotation(MetaObject.ANNOT_RESOURCE, p.toAbsolutePath().toString());
                        return spec;
                    } catch (IOException e) {
                        throw new JikkouException(
                                "Failed to read specification from '" + p + "'. " +
                                "Error : " + e.getMessage()
                        );
                    }
                });
    }

}
