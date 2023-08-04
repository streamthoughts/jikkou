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
public final class YAMLResourceLoader {

    private final ResourceTemplateRenderer renderer;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new {@link YAMLResourceLoader} instance.
     *
     * @param renderer  the {@link ResourceTemplateRenderer} to be used.
     * @param objectMapper  the {@link ObjectMapper} to be used.
     */
    public YAMLResourceLoader(final @NotNull ResourceTemplateRenderer renderer,
                              final @NotNull ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    @NotNull
    public HasItems load(final @NotNull ResourceLoaderInputs inputs) {
        ResourceReaderFactory factory = new ResourceReaderFactory(objectMapper, renderer);

        ResourceReaderOptions readerOptions = new ResourceReaderOptions();

        if (!inputs.getValuesFileLocations().isEmpty()) {
            ValuesLoader loader = new ValuesLoader(objectMapper);
            NamedValue.Set values = loader.load(inputs.getValuesFileLocations());

            readerOptions = readerOptions.withValues(values);
        }

        readerOptions = readerOptions
                .withLabels(NamedValue.setOf(inputs.getLabels()))
                .withValues(NamedValue.setOf(inputs.getValues()))
                .withPattern(inputs.getResourceFilePattern());

        return ResourceLoader.create()
                .withResourceReaderFactory(factory)
                .withResourceReaderOptions(readerOptions)
                .load(inputs.getResourceFileLocations());
    }
}
