/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.api.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
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
    private NamedValue.Set getLabels(final @NotNull ResourceLoaderInputs inputs) {
        return NamedValue.setOf(inputs.getLabels());
    }

    @NotNull
    private NamedValue.Set getValues(final @NotNull ResourceLoaderInputs inputs) {
        NamedValue.Set all = NamedValue.emptySet();
        if (!inputs.getValuesFileLocations().isEmpty()) {
            ValuesLoader loader = new ValuesLoader(objectMapper);
            all = all.with(loader.load(inputs.getValuesFileLocations()));
        }
        all = all.with(inputs.getValues());
        return all;
    }
}
