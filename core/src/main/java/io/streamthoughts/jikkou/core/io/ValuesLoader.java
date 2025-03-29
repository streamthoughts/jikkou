/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderOptions;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.net.URI;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ValuesLoader {

    private final ValuesReaderFactory factory;

    /**
     * Creates a new {@link ValuesLoader} instance.
     */
    public ValuesLoader(final ObjectMapper objectMapper) {
        this.factory = new ValuesReaderFactory(objectMapper);
    }

    public @NotNull NamedValueSet load(final @NotNull List<String> locations,
                                       final @NotNull ValuesReaderOptions options) {
        return locations.stream()
            .map(location -> factory.create(URI.create(location)))
            .map(reader -> reader.readAll(options))
            .reduce(NamedValueSet.emptySet(), NamedValueSet::with);
    }
}
