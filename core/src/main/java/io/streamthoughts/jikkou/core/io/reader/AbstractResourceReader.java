/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.streamthoughts.jikkou.core.models.CoreAnnotations.JKKOU_IO_MANAGED_BY_LOCATION;

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

        resource.getMetadata().addAnnotationIfAbsent(JKKOU_IO_MANAGED_BY_LOCATION, location.toString());
        return resource;
    }
}
