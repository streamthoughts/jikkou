/*
 * Copyright 2022 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.io.reader;

import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class DirectoryResourceReader implements ResourceReader {

    private final Path directory;

    private final ResourceReaderFactory readerFactory;

    /**
     * Creates a new {@link DirectoryResourceReader} instance.
     *
     * @param directory the directory from which to read all resources.
     */
    public DirectoryResourceReader(@NotNull final Path directory,
                                   @NotNull final ResourceReaderFactory readerFactory) {
        this.directory = Objects.requireNonNull(directory, "'directory' must not be null");
        this.readerFactory = Objects.requireNonNull(readerFactory, "'readerFactory' must not be null");
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("'" + directory + "' is not a directory");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HasMetadata> readAllResources(@NotNull ResourceReaderOptions options) throws JikkouRuntimeException {
        return IOUtils.findMatching(directory, options.pattern())
                .parallelStream()
                .flatMap(path -> {
                    try (var reader = readerFactory.create(path.toUri())) {
                        return reader.readAllResources(options).stream();
                    }
                }).toList();
    }
}
