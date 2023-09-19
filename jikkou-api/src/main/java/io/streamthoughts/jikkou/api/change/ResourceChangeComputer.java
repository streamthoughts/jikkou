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
package io.streamthoughts.jikkou.api.change;

import static io.streamthoughts.jikkou.api.change.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.change.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.change.ChangeType.IGNORE;
import static io.streamthoughts.jikkou.api.change.ChangeType.UPDATE;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class ResourceChangeComputer<T extends HasMetadata, V, C extends Change>
        extends AbstractChangeComputer<T, V, C> {


    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public ResourceChangeComputer(final @NotNull ChangeKeyMapper<T> keyMapper,
                                  final @NotNull ChangeValueMapper<T, V> valueMapper,
                                  boolean deleteOrphans) {
        super(keyMapper, valueMapper, deleteOrphans);
    }

    /** {@inheritDoc} **/
    @Override
    protected ObjectMeta getObjectMetadata(T before, T after) {
        if (after != null) return after.getMetadata();
        if (before != null) return before.getMetadata();
        throw new IllegalArgumentException("both arguments are null");
    }

    /** {@inheritDoc} **/
    @Override
    protected ChangeType getChangeType(T before, T after) {
        if (before == null && after == null)
            return IGNORE;

        if (before == null)
            return JikkouMetadataAnnotations.isAnnotatedWithDelete(after) ? IGNORE : ADD;

        if (after == null)
            return DELETE;

        return JikkouMetadataAnnotations.isAnnotatedWithDelete(after) ? DELETE : UPDATE;
    }

    public abstract List<C> buildChangeForDeleting(V before);

    public abstract List<C> buildChangeForUpdating(V before, V after);

    public abstract List<C> buildChangeForNone(V before, V after);

    public abstract List<C> buildChangeForCreating(V after);

    public static <T extends HasMetadata> ChangeKeyMapper<T> metadataNameKeyMapper() {
        return new ChangeKeyMapper<T>() {

            @Override
            public @NotNull Object apply(@NotNull T object) {
                return object.getMetadata().getName();
            }
        };
    }
}
