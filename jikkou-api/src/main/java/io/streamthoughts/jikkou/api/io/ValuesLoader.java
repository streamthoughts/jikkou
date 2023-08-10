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
import io.streamthoughts.jikkou.api.error.InvalidResourceFileException;
import io.streamthoughts.jikkou.api.model.NamedValue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValuesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ValuesLoader.class);

    private final ObjectMapper objectMapper;

    /**
     * Creates a new {@link ValuesLoader} instance.
     *
     * @param objectMapper the {@link ObjectMapper}. Cannot be null.
     */
    public ValuesLoader(final @NotNull ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public @NotNull NamedValue.Set load(final @NotNull List<String> locations) {
        return locations
                .parallelStream()
                .flatMap(this::load)
                .sorted(Comparator.comparing(ValuesFile::file))
                .map(ValuesFile::values)
                .reduce(NamedValue.emptySet(), NamedValue.Set::with);
    }

    private @NotNull Stream<ValuesFile> load(final String location) {
        try {
            Path path = Path.of(location);
            if (Files.size(path) > 0) {
                try (InputStream stream = Files.newInputStream(path)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> values = objectMapper.readValue(stream, Map.class);
                    return Stream.of(new ValuesFile(location, NamedValue.setOf(values)));
                }
            } else {
                LOG.debug("Ignore values from '{}'. File is empty.", location);
                return Stream.empty();
            }
        } catch (NoSuchFileException e) {
            throw new InvalidResourceFileException(
                    null,
                    String.format("Failed to read '%s': No such file.", location)
            );
        } catch (IOException e) {
            throw new InvalidResourceFileException(
                    null,
                    String.format("Failed to read '%s': %s", location, e.getLocalizedMessage())
            );
        }
    }

    private record ValuesFile(String file, NamedValue.Set values) { }
}
