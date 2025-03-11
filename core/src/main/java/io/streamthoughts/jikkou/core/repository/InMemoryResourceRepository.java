/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InMemoryResourceRepository implements ResourceRepository {

    private final List<HasMetadata> resources;

    /**
     * Creates a new {@link InMemoryResourceRepository} instance.
     */
    public InMemoryResourceRepository() {
        this(new LinkedList<>());
    }

    /**
     * Creates a new {@link InMemoryResourceRepository} instance.
     *
     * @param resources The list of resources.
     */
    public InMemoryResourceRepository(final List<HasMetadata> resources) {
        this.resources = resources;
    }

    public void addResource(HasMetadata resource) {
        this.resources.add(resource);
    }

    /** {@inheritDoc} **/
    @Override
    public List<? extends HasMetadata> all() {
        return Collections.unmodifiableList(resources);
    }
}
