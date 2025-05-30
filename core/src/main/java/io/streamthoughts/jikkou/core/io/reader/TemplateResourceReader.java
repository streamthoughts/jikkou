/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceException;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceFileException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectTemplate;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.core.template.TemplateBindings;
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
    public List<HasMetadata> readAll(@NotNull final ResourceReaderOptions options) throws JikkouRuntimeException {

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
            throw new InvalidResourceException(message, e);
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
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            mapper.writeValue(os, object);
            os.flush();
            return new ByteArrayInputStream(os.toByteArray());
        }
    }

    private InputStream renderTemplate(final @NotNull InputStream templateInputStream,
                                       final @NotNull TemplateBindings templateBindings) {
        final String resource;

        try {
            resource = new String(templateInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidResourceFileException(
                    location,
                    e.getLocalizedMessage());
        }

        if (resource.isEmpty()) {
            throw new InvalidResourceFileException(
                    location,
                    String.format("Resource file at location '%s' is empty", location)
            );
        }

        final String rendered = renderer.render(resource, location, templateBindings);

        return IOUtils.openStream(rendered);
    }
}
