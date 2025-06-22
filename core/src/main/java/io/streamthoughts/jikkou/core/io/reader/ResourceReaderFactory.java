/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import java.io.InputStream;
import java.net.URI;
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
        return create(inputStream, null);
    }

    /**
     * Creates a new {@link ResourceReader} to read resource(s) from the given {@link InputStream}.
     *
     * @param inputStream the {@link InputStream} from which resource(s) must be read.
     * @param location    the {@link URI} of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final InputStream inputStream, final URI location) {
        return getResourceReader(() -> inputStream, location);
    }

    /**
     * Creates a new {@link ResourceReader} to read the resource(s) at the given location.
     *
     * @param location the {@link URI} of the resource(s) to be read.
     * @return a new {@link ResourceReader}.
     */
    public ResourceReader create(final URI location) {
        return IOUtils.isLocalDirectory(location)?
                !location.isAbsolute() ?
                        new DirectoryResourceReader(Path.of(location.getPath()), this) :
                        new DirectoryResourceReader(Path.of(location), this)
                :
                getResourceReader(() -> IOUtils.newInputStream(location), location);
    }

    @NotNull
    private ResourceReader getResourceReader(@NotNull Supplier<InputStream> inputStreamSupplier,
                                             @Nullable URI location) {
        return isTemplateEnable && templateRenderer != null ?
            new TemplateResourceReader(templateRenderer, inputStreamSupplier, objectMapper, location) :
            new InputStreamResourceReader(inputStreamSupplier, objectMapper, location);
    }
}
