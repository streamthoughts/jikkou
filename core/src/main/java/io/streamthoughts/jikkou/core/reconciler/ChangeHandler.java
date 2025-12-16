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
public interface ChangeHandler {

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
    List<ChangeResponse> handleChanges(@NotNull final List<ResourceChange> changes);

    /**
     * Gets a textual description for the given {@link Change}.
     *
     * @param change The resource change.
     * @return The textual description.
     */
    TextDescription describe(@NotNull final ResourceChange change);

    class None extends BaseChangeHandler {

        private final Function<ResourceChange, TextDescription> description;

        public None(Function<ResourceChange, TextDescription> description) {
            super(Operation.NONE);
            this.description = description;
        }

        @Override
        public TextDescription describe(@NotNull ResourceChange change) {
            return description.apply(change);
        }

        @Override
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream().map(ChangeResponse::new).toList();
        }
    }
}
