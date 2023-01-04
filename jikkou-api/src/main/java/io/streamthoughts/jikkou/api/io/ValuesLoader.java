/*
 * Copyright 2023 StreamThoughts.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.NamedValue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValuesLoader {

    private final ObjectMapper objectMapper;

    public ValuesLoader() {
        this(Jackson.YAML_OBJECT_MAPPER);
    }

    public ValuesLoader(final @NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ValuesLoader.class);

    public @NotNull NamedValue.Set load(final @NotNull List<String> locations) {
        return locations
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
                    @SuppressWarnings("unchecked")
                    Map<String, Object> values = objectMapper.readValue(src, Map.class);
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
