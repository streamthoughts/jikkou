/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.HasMetadata;
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


        static <V extends HasMetadata> KeyMapper<V, String> byName() {
            return value -> value.getMetadata().getName();
        }
    }

    /**
     * Interface for creating change object.
     *
     * @param <K> The resource-key type.
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
