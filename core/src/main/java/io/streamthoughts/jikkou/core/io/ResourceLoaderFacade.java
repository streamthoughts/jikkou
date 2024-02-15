/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class ResourceLoaderFacade {

    private final ResourceTemplateRenderer renderer;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new {@link ResourceLoaderFacade} instance.
     *
     * @param renderer  the {@link ResourceTemplateRenderer}. Cannot be {@code null}.
     * @param objectMapper  the {@link ObjectMapper}. Cannot be {@code null}.
     */
    public ResourceLoaderFacade(final @NotNull ResourceTemplateRenderer renderer,
                                final @NotNull ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    @NotNull
    public HasItems load(final @NotNull ResourceLoaderInputs inputs) {
        ResourceLoader loader = new ResourceLoader(
                createResourceReaderFactory(),
                createResourceReaderOptions(inputs)
        );

        return loader.load(inputs.getResourceFileLocations());
    }

    @NotNull
    private ResourceReaderOptions createResourceReaderOptions(@NotNull ResourceLoaderInputs inputs) {
        return new ResourceReaderOptions()
                .withLabels(getLabels(inputs))
                .withValues(getValues(inputs))
                .withPattern(inputs.getResourceFilePattern());
    }

    @NotNull
    private ResourceReaderFactory createResourceReaderFactory() {
        return new ResourceReaderFactory(objectMapper, renderer);
    }

    @NotNull
    private NamedValueSet getLabels(final @NotNull ResourceLoaderInputs inputs) {
        return NamedValueSet.setOf(inputs.getLabels());
    }

    @NotNull
    private NamedValueSet getValues(final @NotNull ResourceLoaderInputs inputs) {
        NamedValueSet all = NamedValueSet.emptySet();
        if (!inputs.getValuesFileLocations().isEmpty()) {
            ValuesLoader loader = new ValuesLoader(objectMapper);
            all = all.with(loader.load(inputs.getValuesFileLocations()));
        }
        all = all.with(inputs.getValues());
        return all;
    }
}
