/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for executing reconciliation changes.
 */
public interface ChangeExecutor<C extends ResourceChange> {

    /**
     * Executes all the changes attached to this executor with the specified handlers.
     *
     * @param handlers The list of ChangeHandler.
     * @return The list of ChangeResult.
     * @throws IllegalArgumentException if more than one handler is provided for a same {@link Operation}.
     * @throws NullPointerException     if the list of handlers is {@code null}.
     */
    List<ChangeResult> applyChanges(@NotNull final List<? extends ChangeHandler<C>> handlers);

    /**
     * Gets the list of changes that will be applied by the executor.
     *
     * @return The list of HasMetadataChange.
     */
    @NotNull List<C> changes();

}
