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
package io.streamthoughts.kafka.specs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Class used to read a Kafka cluster specification a from a YAML input file.
 */
public class YAMLClusterSpecReader implements ClusterSpecReader {

    private static final Logger LOG = LoggerFactory.getLogger(YAMLClusterSpecReader.class);

    static final ClusterSpecReaders CURRENT_VERSION = ClusterSpecReaders.VERSION_1;

    public enum ClusterSpecReaders implements ClusterSpecReader {
        VERSION_1("1") {
            @Override
            public ClusterSpec read(final InputStream specification) throws InvalidSpecificationException {
                try {
                    return Jackson.OBJECT_MAPPER.readValue(specification, ClusterSpec.class);
                } catch (IOException e) {
                    throw new InvalidSpecificationException("Invalid specification file: " + e.getLocalizedMessage());
                }
            }
        };

        private final String version;

        /**
         * Creates a new
         * @param version   the string version.
         */
        ClusterSpecReaders(final String version) {
            this.version = version;
        }

        public String version() {
            return version;
        }

        public abstract ClusterSpec read(final InputStream specification) throws InvalidSpecificationException;

        public static Optional<ClusterSpecReaders> getVersionFromString(final String version) {
            for (ClusterSpecReaders e : ClusterSpecReaders.values()) {
                if (e.version().endsWith(version)) {
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
    public ClusterSpec read(final InputStream stream) {
        try {
            final String specification = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (specification.isEmpty()) {
                throw new InvalidSpecificationException("Empty specification file");
            }
            final Versioned versioned = Jackson.OBJECT_MAPPER.readValue(
                    newInputStream(specification),
                    Versioned.class
            );

            if (versioned.version().isEmpty()) {
                LOG.warn(
                        "No version found in input specification file, fallback on the current version {}",
                        CURRENT_VERSION.version
                );
                return CURRENT_VERSION.read(newInputStream(specification));
            }

            final Optional<ClusterSpecReaders> specVersion = ClusterSpecReaders.getVersionFromString(versioned.toString());
            return specVersion.orElseGet(() -> {
                LOG.info(
                        "Unknown version '{}', using current version {}",
                        versioned,
                        CURRENT_VERSION
                );
                return CURRENT_VERSION;
            }).read(newInputStream(specification));
        } catch (IOException e) {
            throw new InvalidSpecificationException(e.getLocalizedMessage());
        }
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
