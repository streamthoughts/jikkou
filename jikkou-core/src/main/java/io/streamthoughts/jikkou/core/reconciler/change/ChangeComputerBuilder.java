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

import java.util.Optional;

/**
 * Interface for building a default {@link ChangeComputer}.
 *
 * @param <K> The type of the state key.
 * @param <V> The type of the state value.
 * @param <R> The type of the state change.
 */
public interface ChangeComputerBuilder<K, V, R> {

    /**
     * Specify whether orphan state should be marked as deleted.
     *
     * @param isDeleteOrphans Set to {@code true} to remove orphan resources. Otherwise {@code false} to ignore them.
     * @return {@code this}
     */
    ChangeComputerBuilder<K, V, R> withDeleteOrphans(boolean isDeleteOrphans);

    /**
     * Sets the {@link KeyMapper}.
     *
     * @param mapper The {@link KeyMapper}.
     * @return {@code this}
     */
    ChangeComputerBuilder<K, V, R> withKeyMapper(KeyMapper<V, K> mapper);

    /**
     * Sets the {@link ChangeFactory}.
     *
     * @param mapper The {@link ChangeFactory}.
     * @return {@code this}
     */
    ChangeComputerBuilder<K, V, R> withChangeFactory(ChangeFactory<K, V, R> mapper);

    /**
     * Builds a new ResourceDataChangeComputer.
     *
     * @return {@code this}
     */
    ChangeComputer<V, R> build();

    /**
     * Interface for mapping a data state to a unique identifier.
     *
     * @param <V> The data object type.
     */
    @FunctionalInterface
    interface KeyMapper<V, K> {

        /**
         * Map the given state value to a unique key.
         *
         * @param value The state value.
         * @return The state ID.
         */
        K apply(V value);
    }

    /**
     * Interface for creating change object.
     *
     * @param <V> The resource type.
     */
    interface ChangeFactory<K, V, R> {

        /**
         * Creates a change representation for the given object states.
         *
         * @param key    The key of the state.
         * @param before The state of the data before the operation. Can be null.
         * @param after  The state of the data after the operation. Can be null.
         * @return The optional change.
         */
        Optional<R> createChange(K key, V before, V after);

    }
}
