/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.Optional;

public class ResourceChangeFactory<K, V extends HasMetadata, R> implements ChangeComputerBuilder.ChangeFactory<K, V, R> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<R> createChange(K key, V before, V after) {
        R result;
        if (before == null) {
            result = isResourceForDeletion(after) ? null : createChangeForCreate(key, after);
        } else if (after == null) {
            result = createChangeForDelete(key, before);
        } else if (isResourceForDeletion(after)) {
            result = createChangeForDelete(key, before);
        } else {
            result = createChangeForUpdate(key, before, after);
        }
        return Optional.ofNullable(result);
    }

    private <V extends HasMetadata> boolean isResourceForDeletion(V resource) {
        return CoreAnnotations.isAnnotatedWithDelete(resource);
    }

    /**
     * Creates a change object for the given states. By default, this method return {@code null}.
     *
     * @param key    The key of the state.
     * @param before The state before the operation.
     * @return The change object.
     */
    public R createChangeForDelete(K key, V before) {
        return null;
    }

    /**
     * Creates a change object for the given states. By default, this method return {@code null}.
     *
     * @param key    The key of the state.
     * @param before The state before the operation.
     * @param after  The state after the operation.
     * @return The change object.
     */
    public R createChangeForUpdate(K key, V before, V after) {
        return null;
    }

    /**
     * Creates a change object for the given state. By default, this method return {@code null}.
     *
     * @param key   The key of the state.
     * @param after The state after the operation.
     * @return The change object.
     */
    public R createChangeForCreate(K key, V after) {
        return null;
    }
}
