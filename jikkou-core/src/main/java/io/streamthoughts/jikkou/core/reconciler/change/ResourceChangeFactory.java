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
