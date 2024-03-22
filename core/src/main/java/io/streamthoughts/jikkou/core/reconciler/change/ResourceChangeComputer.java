/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputerBuilder.KeyMapper;
import java.util.List;

public class ResourceChangeComputer<K, V extends HasMetadata, R> implements ChangeComputer<V, R> {

    private final ChangeComputer<V, R> delegate;

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public ResourceChangeComputer(final KeyMapper<V, K> keyMapper,
                                  final ResourceChangeFactory<K, V, R> changeFactory) {

        this(keyMapper, changeFactory, false);
    }

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public ResourceChangeComputer(final KeyMapper<V, K> keyMapper,
                                  final ResourceChangeFactory<K, V, R> changeFactory,
                                  boolean deleteOrphans) {

        this.delegate = ChangeComputer.<K, V, R>builder()
                .withDeleteOrphans(deleteOrphans)
                .withKeyMapper(keyMapper)
                .withChangeFactory(changeFactory)
                .build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<R> computeChanges(Iterable<V> actualStates,
                                  Iterable<V> expectedStates) {
        return delegate.computeChanges(actualStates, expectedStates);
    }

}
