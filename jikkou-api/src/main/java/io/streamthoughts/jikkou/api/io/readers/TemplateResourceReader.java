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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.streamthoughts.jikkou.api.error.InvalidResourceFileException;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.GenericResource;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectTemplate;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.api.template.TemplateBindings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TemplateResourceReader extends AbstractResourceReader {

    private final ResourceTemplateRenderer renderer;

    /**
     * Creates a new {@link TemplateResourceReader} instance.
     *
     * @param renderer           the render to be used for rendering resource template.
     * @param resourceSupplier   the {@link InputStream} from which to read resources.
     */
    public TemplateResourceReader(@NotNull final ResourceTemplateRenderer renderer,
                                  @NotNull final Supplier<InputStream> resourceSupplier,
                                  @NotNull final ObjectMapper objectMapper) {
        this(renderer, resourceSupplier, objectMapper, null);
    }
    /**
     * Creates a new {@link TemplateResourceReader} instance.
     *
     * @param renderer           the render to be used for rendering resource template.
     * @param location the location {@link Path} of the template to read.
     */
    public TemplateResourceReader(@NotNull ResourceTemplateRenderer renderer,
                                  @NotNull final Supplier<InputStream> resourceSupplier,
                                  @NotNull final ObjectMapper objectMapper,
                                  @Nullable final URI location) {
        super(resourceSupplier, location, objectMapper);
        this.renderer = renderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HasMetadata> readAllResources(@NotNull final ResourceReaderOptions options) throws JikkouException {

        var factory =  mapper.getFactory();
        try (var rawInputStream = resourceSupplier.get()) {

            var bindings = TemplateBindings.defaults()
                    .addLabels(options.labels().asMap())
                    .addValues(options.values().asMap());

            // Run first template rendering
            try (var renderedInputStream = renderTemplate(rawInputStream, bindings)) {

                JsonParser parser = factory.createParser(renderedInputStream);

                // Reads all YAML object from
                List<ObjectNode> objects = mapper
                        .readValues(parser, ObjectNode.class)
                        .readAll();

                List<HasMetadata> list = new ArrayList<>();
                for (ObjectNode object : objects) {
                    // Run second template rendering
                    HasMetadata resource = (HasMetadata) renderTemplate(object, options);
                    list.add(mayAddResourceAnnotationForLocation(resource));
                }
                return list;
            }
        } catch (IOException e) {
            var message = String.format(
                    "Failed to parse and/or render resource file at location '%s'. Cause: %s",
                    location,
                    e.getLocalizedMessage()
            );
            throw new InvalidResourceFileException(message, e);
        }
    }

    private Resource renderTemplate(@NotNull final ObjectNode objectNode,
                                    @NotNull final ResourceReaderOptions options) throws IOException {
        GenericResource resource;

        resource = mapper.treeToValue(objectNode, GenericResource.class);

        var localLabels = Optional
                .ofNullable(resource.getMetadata())
                .flatMap(m -> Optional.ofNullable(m.getLabels()))
                .orElse(Collections.emptyMap());

        var localValues = Optional
                .ofNullable(resource.getObjectTemplate())
                .flatMap(ObjectTemplate::optionalValues)
                .orElse(Collections.emptyMap());

        var localBindings = TemplateBindings.defaults()
                .addLabels(options.labels().asMap())
                .addLabels(localLabels)
                .addValues(options.values().asMap())
                .addValues(localValues);

        try (var objectNodeInputStream = objectNodeToInputStreams(mapper, objectNode);
             var renderedInputStream = renderTemplate(objectNodeInputStream, localBindings)) {
            return mapper.readValue(renderedInputStream, Resource.class);
        }
    }

    @NotNull
    private static InputStream objectNodeToInputStreams(ObjectMapper mapper,
                                                        ObjectNode object) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mapper.writeValue(os, object);
        os.flush();
        return new ByteArrayInputStream(os.toByteArray());
    }

    private InputStream renderTemplate(final @NotNull InputStream templateInputStream,
                                       final @NotNull TemplateBindings templateBindings) {
        final String specification;

        try {
            specification = new String(templateInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidResourceFileException(e.getLocalizedMessage());
        }

        if (specification.isEmpty()) {
            throw new InvalidResourceFileException(
                    String.format("Resource file at location '%s' is empty", location)
            );
        }

        final String rendered = renderer.render(specification, templateBindings);

        return newInputStream(rendered);
    }

    private InputStream newInputStream(final String specification) {
        return new ByteArrayInputStream(specification.getBytes(StandardCharsets.UTF_8));
    }
}
