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

import static io.streamthoughts.jikkou.api.model.ObjectMeta.ANNOT_RESOURCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.streamthoughts.jikkou.api.error.InvalidResourceFileException;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.ResourceReader;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputStreamResourceReader implements ResourceReader {

    private final URI location;
    private final Supplier<InputStream> resourceSupplier;

    private final ObjectMapper mapper;

    /**
     * Creates a new {@link InputStreamResourceReader} instance.
     *
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     */
    public InputStreamResourceReader(@NotNull final Supplier<InputStream> resourceSupplier) {
        this(resourceSupplier, null);
    }

    /**
     * Creates a new {@link InputStreamResourceReader} instance.
     *
     * @param location         the location {@link Path} of the template to read.
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     */
    public InputStreamResourceReader(@NotNull final Supplier<InputStream> resourceSupplier,
                                     @Nullable final URI location) {
        this.resourceSupplier = Objects.requireNonNull(resourceSupplier, "'resourceSupplier' should not be null");
        this.location = location;
        this.mapper = Jackson.YAML_OBJECT_MAPPER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HasMetadata> readAllResources(@NotNull final ResourceReaderOptions options) throws JikkouException {

        var factory = (YAMLFactory) Jackson.YAML_OBJECT_MAPPER.getFactory();
        try (var rawInputStream = resourceSupplier.get()) {

            YAMLParser parser = factory.createParser(rawInputStream);
            // Reads all YAML object from
            List<ObjectNode> objects = mapper
                    .readValues(parser, ObjectNode.class)
                    .readAll();

            List<HasMetadata> list = new ArrayList<>();
            for (ObjectNode object : objects) {
                HasMetadata resource = (HasMetadata) mapper.treeToValue(object, Resource.class);
                mayAddResourceAnnotationForLocation(resource);
                list.add(resource);
            }
            return list;

        } catch (IOException e) {
            throw new InvalidResourceFileException(
                    String.format("Failed to parse and/or render resource file at location '%s'", location),
                    e
            );
        }
    }

    private void mayAddResourceAnnotationForLocation(final Resource resource) {
        if (location != null && resource instanceof HasMetadata) {
            ObjectMeta om = ((HasMetadata) resource)
                    .optionalMetadata()
                    .or(() -> Optional.of(ObjectMeta.builder().build()))
                    .map(m -> ObjectMeta
                            .builder()
                            .withName(m.getName())
                            .withLabels(m.getLabels())
                            .withAnnotations(m.getAnnotations())
                            .withAnnotation(ANNOT_RESOURCE, location.toString())
                            .build()
                    ).get();
            ((HasMetadata) resource).withMetadata(om);
        }
    }
}
