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
package io.streamthoughts.kafka.specs.io;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.internal.IOUtils;
import io.streamthoughts.kafka.specs.model.MetaObject;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class SpecFileLoader {

    private static final Logger LOG = LoggerFactory.getLogger(SpecFileLoader.class);

    private String pattern;

    private final SpecFileReader reader;

    private Map<String, Object> vars = new HashMap<>();
    private Map<String, Object> labels = new HashMap<>();

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

    public SpecFileLoader withVars(@NotNull final Map<String, Object> vars) {
        this.vars = vars;
        return this;
    }

    public List<V1SpecFile> loadFromPath(final @NotNull List<String> files) {
        if (files.isEmpty()) {
            throw new JikkouException("No specification file loaded");
        }

        List<V1SpecFile> loaded = files.stream()
                .flatMap(file -> {
                    try {
                        if (file.startsWith("http://") || file.startsWith("https://")) {
                            InputStream is = new URL(file).openStream();
                            return Stream.of(parse(is, file));
                        }
                        return loadFromPath(Path.of(file));
                    } catch (Exception e) {
                        throw new JikkouException(
                                "Failed to read specification from '" + file + "'. " +
                                        "Error : " + e.getMessage()
                        );
                    }
                })
                .toList();
        if (loaded.isEmpty()) {
            throw new JikkouException("No specification file loaded");
        }

        return loaded;
    }

    @NotNull
    private Stream<V1SpecFile> loadFromPath(final Path path) {
        List<Path> matching = Files.isDirectory(path) ?
                IOUtils.findMatching(path, pattern) :
                List.of(path);

        return matching.parallelStream()
                .map(p -> {
                    LOG.info("Loading specification file from '{}'", p);
                    try {
                        InputStream is = Files.newInputStream(p);
                        return parse(is, p.toAbsolutePath().toString());
                    } catch (IOException e) {
                        throw new JikkouException(
                                "Failed to read specification from '" + p + "'. " +
                                "Error : " + e.getMessage()
                        );
                    }
                });
    }

    private V1SpecFile parse(@NotNull final InputStream is, @NotNull final String file) {
        Map<String, Object> templatingVars = JikkouParams.TEMPLATING_VARS_CONFIG.get(JikkouConfig.get());
        templatingVars.putAll(vars);
        V1SpecFile parsed = reader.read(is, templatingVars, labels);
        parsed.metadata().setAnnotation(MetaObject.ANNOT_RESOURCE, file);
        return parsed;
    }
}
