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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.streamthoughts.jikkou.api.error.InvalidResourceException;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputStreamResourceReader extends AbstractResourceReader {

    /**
     * Creates a new {@link InputStreamResourceReader} instance.
     *
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     */
    public InputStreamResourceReader(@NotNull final Supplier<InputStream> resourceSupplier,
                                     @NotNull final ObjectMapper objectMapper) {
        this(resourceSupplier, objectMapper, null);
    }

    /**
     * Creates a new {@link InputStreamResourceReader} instance.
     *
     * @param location         the location {@link Path} of the template to read.
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     */
    public InputStreamResourceReader(@NotNull final Supplier<InputStream> resourceSupplier,
                                     @NotNull final ObjectMapper objectMapper,
                                     @Nullable final URI location) {
        super(resourceSupplier, location, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HasMetadata> readAllResources(@NotNull final ResourceReaderOptions options) throws JikkouRuntimeException {

        var factory = mapper.getFactory();
        try (var rawInputStream = resourceSupplier.get()) {

            JsonParser parser = factory.createParser(rawInputStream);
            // Reads all YAML object from
            List<ObjectNode> objects = mapper
                    .readValues(parser, ObjectNode.class)
                    .readAll();

            List<HasMetadata> list = new ArrayList<>();
            for (ObjectNode object : objects) {
                HasMetadata resource = (HasMetadata) mapper.treeToValue(object, Resource.class);
                list.add(mayAddResourceAnnotationForLocation(resource));
            }
            return list;

        } catch (IOException e) {
            String errorMessage = location != null ?
                    String.format(
                            "Failed to parse and/or render resource file at location '%s'.",
                            location
                    ) :
                    "Failed to parse and/or render resource file.";
            throw new InvalidResourceException(errorMessage, e);
        }
    }
}
