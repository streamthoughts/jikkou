/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
