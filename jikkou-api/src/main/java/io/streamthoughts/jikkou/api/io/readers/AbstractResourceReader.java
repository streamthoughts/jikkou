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

import static io.streamthoughts.jikkou.api.model.ObjectMeta.ANNOT_RESOURCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.io.ResourceReader;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractResourceReader implements ResourceReader {

    protected final URI location;
    protected final Supplier<InputStream> resourceSupplier;
    protected final ObjectMapper mapper;

    /**
     * Creates a new {@link InputStreamResourceReader} instance.
     *
     * @param location         the location {@link Path} of the template to read.
     * @param resourceSupplier the {@link InputStream} from which to read resources.
     * @param mapper           the {@link ObjectMapper} to be used for reading the resource.
     */
    protected AbstractResourceReader(@NotNull final Supplier<InputStream> resourceSupplier,
                                     @Nullable final URI location,
                                     @NotNull ObjectMapper mapper) {
        this.resourceSupplier = Objects.requireNonNull(resourceSupplier, "'resourceSupplier' must not be null");
        this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null");
        this.location = location;
    }

    protected HasMetadata mayAddResourceAnnotationForLocation(@NotNull final HasMetadata resource) {
        if (location == null) {
            return resource;
        }

        ObjectMeta om = resource
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
        return resource.withMetadata(om);

    }
}
