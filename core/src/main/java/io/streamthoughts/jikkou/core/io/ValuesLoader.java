/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderOptions;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ValuesLoader {

    public static @NotNull NamedValueSet loadFromLocations(final @NotNull List<String> locations,
                                                           final @NotNull ValuesReaderOptions options) {
        ValuesReaderFactory factory = new ValuesReaderFactory(Jackson.YAML_OBJECT_MAPPER);
        return locations.stream()
            .map(location -> factory.create(URI.create(location)))
            .map(reader -> reader.readAll(options))
            .reduce(NamedValueSet.emptySet(), NamedValueSet::with);
    }

    public static @NotNull NamedValueSet loadFromInputStreams(final @NotNull List<Pair<InputStream, URI>> streams,
                                                              final @NotNull ValuesReaderOptions options) {
        ValuesReaderFactory factory = new ValuesReaderFactory(Jackson.YAML_OBJECT_MAPPER);
        return streams.stream()
            .map(pair -> factory.create(pair._1(), pair._2()))
            .map(reader -> reader.readAll(options))
            .reduce(NamedValueSet.emptySet(), NamedValueSet::with);
    }
}
