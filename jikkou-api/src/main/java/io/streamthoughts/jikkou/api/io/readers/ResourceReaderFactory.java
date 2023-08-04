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
package io.streamthoughts.jikkou.api.io.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.error.InvalidResourceFileException;
import io.streamthoughts.jikkou.api.io.ResourceReader;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory class to create new {@link ResourceReader}.
 *
 * @see InputStreamResourceReader
 * @see TemplateResourceReader
 * @see DirectoryResourceReader
 */
public class ResourceReaderFactory {

    private final ObjectMapper objectMapper;

    private final ResourceTemplateRenderer templateRenderer;

    private final boolean isTemplateEnable;
    
    /**
     * Creates a new {@link ResourceReaderFactory} instance.
     *
     * @param objectMapper  the {@link ObjectMapper}.
     */
    public ResourceReaderFactory(@NotNull ObjectMapper objectMapper) {
        this(objectMapper, null);
    }

    /**
     * Creates a new {@link ResourceReaderFactory} instance.
     *
     * @param objectMapper  the {@link ObjectMapper}.
     * @param renderer      the {@link ResourceTemplateRenderer}.
     */
    public ResourceReaderFactory(@NotNull ObjectMapper objectMapper,
                                 @Nullable ResourceTemplateRenderer renderer) {
        this(objectMapper, renderer, renderer != null);
    }


    /**
     * Creates a new {@link ResourceReaderFactory} instance.
     *
     * @param objectMapper  the {@link ObjectMapper}.
     * @param renderer      the {@link ResourceTemplateRenderer}.
     */
    public ResourceReaderFactory(@NotNull ObjectMapper objectMapper,
                                 @Nullable ResourceTemplateRenderer renderer,
                                 boolean isTemplateEnable
                                 ) {
        this.templateRenderer = renderer;
        this.isTemplateEnable = isTemplateEnable;
        this.objectMapper = objectMapper;
        if (isTemplateEnable && templateRenderer == null) {
            throw new IllegalArgumentException("A render must be configured when templating is enabled");
        }
    }

    /**
     * Creates a new {@link ResourceReader} to read resource(s) from the given {@link InputStream}.
     *
     * @param inputStream the {@link InputStream} from which resource(s) must be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final InputStream inputStream) {
        return getResourceReader(() -> inputStream, null);
    }

    /**
     * Creates a new {@link ResourceReader} to read the resource(s) at the given location.
     *
     * @param location the {@link URI} of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final URI location) {
        return IOUtils.isLocalDirectory(location)?
            new DirectoryResourceReader(Path.of(location.getPath()), this) :
            getResourceReader(() -> newInputStream(location), location);
    }

    @NotNull
    private ResourceReader getResourceReader(@NotNull Supplier<InputStream> inputStreamSupplier,
                                             @Nullable URI location) {
        return isTemplateEnable ?
            new TemplateResourceReader(templateRenderer, inputStreamSupplier, objectMapper, location) :
            new InputStreamResourceReader(inputStreamSupplier, objectMapper, location);
    }

    @NotNull
    private static InputStream newInputStream(final URI location) {
        try {
            return IOUtils.openStream(location);
        } catch (RuntimeException e) {
            Throwable t = e.getCause() != null ? e.getCause() : e;
            if (t instanceof NoSuchFileException) {
                throw new InvalidResourceFileException(
                        location,
                        String.format("Failed to read '%s': No such file or directory.", location)
                );
            } else {
                throw new InvalidResourceFileException(
                        location,
                        String.format("Failed to read '%s': %s", location, t.getMessage()));
            }
        }
    }
}
