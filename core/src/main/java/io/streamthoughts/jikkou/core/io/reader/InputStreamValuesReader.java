/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputStreamValuesReader implements ValuesReader {

    private final URI location;
    private final Supplier<InputStream> resourceSupplier;
    private final ObjectMapper mapper;


    /**
     * Creates a new {@link InputStreamValuesReader} instance.
     *
     * @param location         the location {@link Path} of the template to read.
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     */
    public InputStreamValuesReader(@NotNull final Supplier<InputStream> resourceSupplier,
                                   @NotNull final ObjectMapper mapper,
                                   @Nullable final URI location) {
        this.resourceSupplier = Objects.requireNonNull(resourceSupplier, "'resourceSupplier' must not be null");
        this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null");
        this.location = location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedValueSet readAll(final ValuesReaderOptions options) throws JikkouRuntimeException {
        return load()
            .sorted(Comparator.comparing(ValuesFile::file))
            .map(ValuesFile::values)
            .reduce(NamedValueSet.emptySet(), NamedValueSet::with);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Stream<ValuesFile> load() {
        try (InputStream stream = resourceSupplier.get()) {
            Map<String, Object> values = mapper.readValue(stream, Map.class);
            return Stream.of(new ValuesFile(location, NamedValueSet.setOf(values)));
        } catch (IOException e) {
            String errorMessage = location != null ?
                "Failed to parse values file at location '%s'.".formatted(location) :
                "Failed to parse values file file.";
            throw new InvalidResourceException(errorMessage, e);
        }
    }

    record ValuesFile(URI file, NamedValueSet values) {
    }
}
