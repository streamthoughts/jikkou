/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;

public class LocalResourceRepository implements ResourceRepository {

    private final List<String> locations;
    private final ResourceLoader loader;

    /**
     * Creates a new {@link InMemoryResourceRepository} instance.
     */
    public LocalResourceRepository(final List<String> locations) {
        this.locations = locations;
        this.loader = new ResourceLoader(
            new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER, null),
            ResourceReaderOptions.DEFAULTS
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends HasMetadata> all() {
        return loader.load(locations).getItems();
    }
}
