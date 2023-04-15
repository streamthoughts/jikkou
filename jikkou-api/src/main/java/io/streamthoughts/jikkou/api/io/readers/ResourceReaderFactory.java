/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.io.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.io.ResourceReader;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default interface to read a cluster specification.
 */
public final class ResourceReaderFactory {

    private ResourceTemplateRenderer templateRenderer;

    private boolean isTemplateEnable = false;

    private ObjectMapper objectMapper;

    public ResourceReaderFactory setObjectMapper(final @NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public ResourceReaderFactory setTemplateRenderer(final @NotNull ResourceTemplateRenderer renderer) {
        this.templateRenderer = renderer;
        return this;
    }

    public ResourceReaderFactory setTemplateEnable(boolean templateEnable) {
        isTemplateEnable = templateEnable;
        return this;
    }

    /**
     * Creates a new {@link ResourceReader} to read the resource(s) at the given {@link URI}.
     *
     * @param location the {@link URI} of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final URI location) {
        String scheme = location.getScheme();

        if (scheme == null) {
            return create(Paths.get(location.getPath()));
        }

        if (scheme.equalsIgnoreCase("file")) {
            return create(Paths.get(location));
        }

        if (scheme.equalsIgnoreCase("http") ||
                scheme.equalsIgnoreCase("https")) {
            return createTemplateResourceLoader(newInputStream(location), location);
        }

        throw new JikkouRuntimeException("Resource location scheme '" + scheme + "' is not supported");
    }

    /**
     * Creates a new {@link ResourceReader} to read the resource(s) from the given {@link InputStream}.
     *
     * @param location the location path of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final Path location) {
        return Files.isDirectory(location) ?
                new DirectoryResourceReader(location, this) :
                options -> {
                    var reader = isTemplateEnable ?
                            createTemplateResourceLoader(newInputStream(location), location.toUri()) :
                            createInputStreamResourceReader(location);

                    try (reader) {
                        return reader.readAllResources(options);
                    }
                };

    }

    @NotNull
    private InputStreamResourceReader createInputStreamResourceReader(@NotNull Path location) {
        return new InputStreamResourceReader(
                () -> newInputStream(location),
                objectMapper,
                location.toUri()
        );
    }

    @NotNull
    private TemplateResourceReader createTemplateResourceLoader(final @NotNull InputStream is,
                                                                final @Nullable URI location) {
        return new TemplateResourceReader(templateRenderer, () -> is, objectMapper, location);
    }

    public ResourceReader create(final InputStream inputStream) {
        return options -> {
            var reader = isTemplateEnable ?
                    createTemplateResourceLoader(inputStream, null) :
                    new InputStreamResourceReader(() -> inputStream, objectMapper);

            try (reader) {
                return reader.readAllResources(options);
            }
        };
    }

    @NotNull
    private static InputStream newInputStream(final URI location) {
        try {
            return location.toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static InputStream newInputStream(final Path location) {
        try {
            return Files.newInputStream(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
