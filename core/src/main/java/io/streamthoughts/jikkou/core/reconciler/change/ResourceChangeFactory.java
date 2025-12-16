/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.List;
import java.util.Optional;

/**
 * Base class for implementing a {@link ChangeComputerBuilder.ChangeFactory}.
 *
 * @param <K>   type of the resource key.
 * @param <V>   type of the resource.
 */
public class ResourceChangeFactory<K, V extends HasMetadata> implements ChangeComputerBuilder.ChangeFactory<K, V, ResourceChange> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<ResourceChange> createChange(K key, V before, V after) {
        ResourceChange result;
        if (before == null) {
            result = isResourceForDeletion(after) ? null : createChangeForCreate(key, after);
        } else if (after == null) {
            result = createChangeForDelete(key, before);
        } else if (isResourceForDeletion(after)) {
            result = createChangeForDelete(key, before);
        } else if (isResourceForReplace(after)) {
            result = createChangeForReplace(key, before, after);
        } else {
            result = createChangeForUpdate(key, before, after);
        }
        return Optional.ofNullable(result);
    }

    private boolean isResourceForDeletion(V resource) {
        return CoreAnnotations.isAnnotatedWithDelete(resource);
    }

    private boolean isResourceForReplace(V resource) {
        return CoreAnnotations.isAnnotatedWithReplace(resource);
    }

    /**
     * Creates a change object for the given states.
     *
     * @param key    The key of the state.
     * @param before The state before the operation.
     * @param after  The state after the operation.
     * @return The change object.
     */
    public ResourceChange createChangeForReplace(K key, V before, V after) {
        return GenericResourceChange
            .builder()
            .withKind("ReplaceChange")
            .withApiVersion("core.jikkou.io/v1")
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.REPLACE)
                .withChanges(List.of(
                    StateChange.delete("delete", createChangeForDelete(key, before)),
                    StateChange.create("create", createChangeForCreate(key, after))
                ))
                .build()
            )
            .build();
    }

    /**
     * Creates a change object for the given states. By default, this method return {@code null}.
     *
     * @param key    The key of the state.
     * @param before The state before the operation.
     * @return The change object.
     */
    public ResourceChange createChangeForDelete(K key, V before) {
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
    public ResourceChange createChangeForUpdate(K key, V before, V after) {
        return null;
    }

    /**
     * Creates a change object for the given state. By default, this method return {@code null}.
     *
     * @param key   The key of the state.
     * @param after The state after the operation.
     * @return The change object.
     */
    public ResourceChange createChangeForCreate(K key, V after) {
        return null;
    }
}
