/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceFileException;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
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

    public @NotNull NamedValueSet load(final @NotNull List<String> locations) {
        return locations
                .parallelStream()
                .flatMap(this::load)
                .sorted(Comparator.comparing(ValuesFile::file))
                .map(ValuesFile::values)
                .reduce(NamedValueSet.emptySet(), NamedValueSet::with);
    }

    private @NotNull Stream<ValuesFile> load(final String location) {
        try {
            Path path = Path.of(location);
            if (Files.size(path) > 0) {
                try (InputStream stream = Files.newInputStream(path)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> values = objectMapper.readValue(stream, Map.class);
                    return Stream.of(new ValuesFile(location, NamedValueSet.setOf(values)));
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

    private record ValuesFile(String file, NamedValueSet values) { }
}
