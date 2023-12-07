/*
 * Copyright 2023 The original authors
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
