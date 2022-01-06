/*
 * Copyright 2020 StreamThoughts.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.kafka.specs.Jackson;
import io.streamthoughts.kafka.specs.error.InvalidSpecsFileException;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.model.V1MetadataObjects;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1TemplateObject;
import io.streamthoughts.kafka.specs.template.TemplateBindings;
import io.streamthoughts.kafka.specs.template.TemplateRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class used to read a Kafka cluster specification a from a YAML input file.
 */
public class YAMLSpecReader implements SpecFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(YAMLSpecReader.class);

    public static final VersionedSpecReader CURRENT_VERSION = VersionedSpecReader.VERSION_1;

    public enum VersionedSpecReader implements SpecFileReader {
        VERSION_1("1") {
            @Override
            public V1SpecFile read(@NotNull final InputStream specification,
                                   @NotNull final Map<String, Object> vars,
                                   @NotNull final Map<String, Object> labels) throws JikkouException {
                try {
                    return Jackson.YAML_OBJECT_MAPPER.readValue(specification, V1SpecFile.class);
                } catch (IOException e) {
                    throw new InvalidSpecsFileException("Invalid specification file: " + e.getLocalizedMessage());
                }
            }
        };

        private final String version;

        /**
         * Creates a new
         *
         * @param version the string version.
         */
        VersionedSpecReader(final String version) {
            this.version = version;
        }

        public String version() {
            return version;
        }

        public static Optional<VersionedSpecReader> getReaderForVersion(final String version) {
            for (VersionedSpecReader e : VersionedSpecReader.values()) {
                if (e.version().startsWith(version)) {
                    return Optional.of(e);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1SpecFile read(@NotNull final InputStream stream,
                           @NotNull final Map<String, Object> overrideVars,
                           @NotNull final Map<String, Object> overrideLabels) {
        try {
            final String specification = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            if (specification.isEmpty()) {
                throw new InvalidSpecsFileException("Empty specification file");
            }

            final Versioned versioned = Jackson.YAML_OBJECT_MAPPER.readValue(
                    newInputStream(specification),
                    Versioned.class
            );

            final VersionedSpecReader reader;
            if (versioned.version().isEmpty()) {
                LOG.warn(
                        "No 'version' was found in input specification file, fallback on the current version {}",
                        CURRENT_VERSION.version
                );
                reader = CURRENT_VERSION;
            } else {
                reader = VersionedSpecReader
                        .getReaderForVersion(versioned.version)
                        .orElseGet(() -> {
                            LOG.warn(
                                    "Unknown version '{}', fallback on the current version '{}'",
                                    versioned.version,
                                    CURRENT_VERSION.version()
                            );
                            return CURRENT_VERSION;
                        });
            }

            return read(reader, specification, overrideVars, overrideLabels);

        } catch (IOException e) {
            throw new InvalidSpecsFileException(e.getLocalizedMessage());
        }
    }

    private V1SpecFile read(final VersionedSpecReader reader,
                            final String specification,
                            final Map<String, Object> overrideVars,
                            final Map<String, Object> overrideLabels) {
        try {

            Map<String, Object> labels = new HashMap<>();
            labels.putAll(parseSpecificationLabels(specification));
            labels.putAll(overrideLabels);

            Map<String, Object> vars = new HashMap<>();
            vars.putAll(parseSpecificationVars(specification));
            vars.putAll(overrideVars);

            final TemplateBindings context = TemplateBindings.defaults()
                    .withLabels(labels)
                    .withVars(vars);

            final String compiled = TemplateRenderer.compile(specification, context);

            V1SpecFile file = reader.read(newInputStream(compiled), vars, labels);
            file.metadata().setLabels(overrideLabels);
            return file;

        } catch (final IOException e) {
            throw new InvalidSpecsFileException(e.getLocalizedMessage());
        }
    }

    private Map<String, Object> parseSpecificationVars(final String specification) throws IOException {
        return Jackson.YAML_OBJECT_MAPPER.readValue(
                newInputStream(specification),
                V1TemplateObject.class
        ).template().vars();
    }

    private Map<String, Object> parseSpecificationLabels(final String specification) throws IOException {
        return Jackson.YAML_OBJECT_MAPPER.readValue(
                newInputStream(specification),
                V1MetadataObjects.class
        ).metadata().getLabels();
    }

    private InputStream newInputStream(final String specification) {
        return new ByteArrayInputStream(specification.getBytes(StandardCharsets.UTF_8));
    }

    private static class Versioned {

        private final String version;

        @JsonCreator
        public Versioned(@JsonProperty("version") final String version) {
            this.version = version;
        }

        public Optional<String> version() {
            return Optional.ofNullable(version);
        }
    }
}
