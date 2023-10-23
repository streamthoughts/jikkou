/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to compute and apply changes required to reconcile resources into a managed system.
 *
 * @param <R> type of the resource managed by this controller.
 * @param <C> type of the change managed by this controller.
 */
@Evolving
public interface BaseResourceController<
        R extends HasMetadata,
        C extends Change>
        extends ResourceController<R, C> {

    /**
     * {@inheritDoc}
     **/
    @Override
    default List<ChangeResult<C>> reconcile(@NotNull final List<R> resources,
                                            @NotNull final ReconciliationMode mode,
                                            @NotNull final ReconciliationContext context) {
        if (!supportedReconciliationModes().contains(mode)) {
            throw new JikkouRuntimeException(
                    String.format(
                            "Reconciliation mode '%s' is not supported by the controller '%s'",
                            mode, getClass().getName()
                    )
            );
        }

        // Compute all changes
        List<HasMetadataChange<C>> filtered = computeReconciliationChanges(resources, mode, context)
                .getItems()
                .stream()
                // Keep only changes relevant for this reconciliation mode.
                .filter(mode::isSupported)
                .collect(Collectors.toList());

        // Execute changes

        return execute(filtered, mode, context.isDryRun());
    }

    /**
     * Execute all changes for the given reconciliation mode.
     *
     * @param changes the list of changes that will be applied eventually.
     * @param dryRun  specify whether this operation should be executed in dry-run.
     * @return the list of all changes applied.
     */
    List<ChangeResult<C>> execute(@NotNull List<HasMetadataChange<C>> changes,
                                  @NotNull final ReconciliationMode mode,
                                  boolean dryRun);

}
