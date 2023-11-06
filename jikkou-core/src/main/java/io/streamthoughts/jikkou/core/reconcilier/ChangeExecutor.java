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
package io.streamthoughts.jikkou.core.reconcilier;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Service interface for executing reconciliation changes.
 *
 * @param <C> type of the change handled by this executor.
 */
public interface ChangeExecutor<C extends Change> {

    /**
     * Executes all the changes attached to this executor with the specified handlers.
     *
     * @param handlers The list of ChangeHandler.
     * @return The list of ChangeResult.
     *
     * @throws IllegalArgumentException if more than one handler is provided for a same {@link ChangeType}.
     * @throws NullPointerException if the list of handlers is {@code null}.
     */
    @NotNull List<ChangeResult<C>> execute(@NotNull final List<? extends ChangeHandler<C>> handlers);

    /**
     * Gets the list of changes that will be applied by the executor.
     *
     * @return The list of HasMetadataChange.
     */
    @NotNull List<HasMetadataChange<C>> changes();

}
