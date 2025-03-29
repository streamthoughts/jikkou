/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class to create new {@link ResourceReader}.
 *
 * @see InputStreamValuesReader
 * @see DirectoryValuesReader
 */
public class ValuesReaderFactory {

    private final ObjectMapper objectMapper;

    /**
     * Creates a new {@link ValuesReaderFactory} instance.
     *
     * @param objectMapper the {@link ObjectMapper}.
     */
    public ValuesReaderFactory(@NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new {@link ResourceReader} to read resource(s) from the given {@link InputStream}.
     *
     * @param inputStream the {@link InputStream} from which resource(s) must be read.
     * @return a new {@link ResourceReader}.
     */
    public ValuesReader create(final InputStream inputStream) {
        return new InputStreamValuesReader(() -> inputStream, objectMapper, null);
    }

    /**
     * Creates a new {@link ResourceReader} to read the resource(s) at the given location.
     *
     * @param location the {@link URI} of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ValuesReader create(final URI location) {
        if (IOUtils.isLocalDirectory(location)) {
            Path path = !location.isAbsolute() ? Path.of(location.getPath()) : Path.of(location);
            return new DirectoryValuesReader(path, this);
        } else {
            return new InputStreamValuesReader(() -> IOUtils.newInputStream(location), objectMapper, null);
        }
    }
}
