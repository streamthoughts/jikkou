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
package io.streamthoughts.jikkou.core.change;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ValueChangeComputer<T extends HasMetadata, V> extends ResourceChangeComputer<T, V, ValueChange<V>> {


    /**
     * Creates a new {@link ValueChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public ValueChangeComputer(final @NotNull ChangeKeyMapper<T> keyMapper,
                               final @NotNull ChangeValueMapper<T, V> valueMapper,
                               boolean deleteOrphans) {
        super(keyMapper, valueMapper, deleteOrphans);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValueChange<V>> buildChangeForDeleting(V before) {
        return List.of(ValueChange.withBeforeValue(before));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValueChange<V>> buildChangeForUpdating(V before, V after) {
        return List.of(ValueChange.with(before, after));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValueChange<V>> buildChangeForNone(V before, V after) {
        return List.of(ValueChange.none(before, after));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValueChange<V>> buildChangeForCreating(V after) {
        return List.of(ValueChange.withAfterValue(after));
    }
}
