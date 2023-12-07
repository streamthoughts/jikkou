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
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for applying resource changes.
 */
public interface ChangeHandler<T extends ResourceChange> {

    /**
     * Gets all the change types supported by this handler.
     *
     * @return The set of {@link Operation}.
     */
    Set<Operation> supportedChangeTypes();

    /**
     * Applies the list of given changes.
     *
     * @param changes the list of objects holding a {@link Change}.
     * @return The list of change application response.
     */
    List<ChangeResponse<T>> handleChanges(@NotNull final List<T> changes);

    /**
     * Gets a textual description for the given {@link Change}.
     *
     * @param change The resource change.
     * @return The textual description.
     */
    TextDescription describe(@NotNull final T change);

    class None<T extends ResourceChange> extends BaseChangeHandler<T> {

        private final Function<T, TextDescription> description;

        public None(Function<T, TextDescription> description) {
            super(Operation.NONE);
            this.description = description;
        }

        @Override
        public TextDescription describe(@NotNull T change) {
            return description.apply(change);
        }

        @Override
        public List<ChangeResponse<T>> handleChanges(@NotNull List<T> changes) {
            return changes.stream().map(ChangeResponse::new).toList();
        }
    }
}
