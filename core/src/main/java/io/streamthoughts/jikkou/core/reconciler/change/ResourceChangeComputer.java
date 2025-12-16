/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputerBuilder.KeyMapper;
import java.util.List;

public class ResourceChangeComputer<K, V extends HasMetadata> implements ChangeComputer<V, ResourceChange> {

    private final ChangeComputer<V, ResourceChange> delegate;

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public ResourceChangeComputer(final KeyMapper<V, K> keyMapper,
                                  final ResourceChangeFactory<K, V> changeFactory) {

        this(keyMapper, changeFactory, false);
    }

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public ResourceChangeComputer(final KeyMapper<V, K> keyMapper,
                                  final ResourceChangeFactory<K, V> changeFactory,
                                  boolean deleteOrphans) {

        this.delegate = ChangeComputer.<K, V, ResourceChange>builder()
                .withDeleteOrphans(deleteOrphans)
                .withKeyMapper(keyMapper)
                .withChangeFactory(changeFactory)
                .build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> computeChanges(Iterable<V> actualStates,
                                  Iterable<V> expectedStates) {
        return delegate.computeChanges(actualStates, expectedStates);
    }

}
