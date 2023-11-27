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

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Reconcile resources.
 *
 * @param <R> type of the resource.
 * @param <C> type of the changes.
 */
public final class Reconcilier<R extends HasMetadata, C extends Change> {

    private final Controller<R, C> controller;

    /**
     * Creates a new {@link Reconcilier} instance.
     *
     * @param controller the controller instance. Cannot be {@code null}.
     */
    public Reconcilier(@NotNull Controller<R, C> controller) {
        this.controller = controller;
    }

    /**
     * Executes the reconciliation of all the given resources.
     *
     * @param resources the list of resources to be reconciled.
     * @param mode      the reconciliation mode to be executed.
     * @param context   the context of the reconciliation.
     * @return the list of changes
     */
    public List<ChangeResult<C>> reconcile(@NotNull List<R> resources,
                                           @NotNull ReconciliationMode mode,
                                           @NotNull ReconciliationContext context) {
        // Check whether this reconciliation mode is supported.
        if (!Controller.supportedReconciliationModes(controller.getClass()).contains(mode)) {
            throw new JikkouRuntimeException(
                    String.format(
                            "Cannot execute reconciliation. Mode '%s' is not supported by controller '%s'",
                            mode,
                            controller.getName()
                    )
            );
        }

        // Plans all the changes to be executed to reconcile the specified resources.
        ResourceListObject<? extends HasMetadataChange<C>> changes = controller.plan(
                resources,
                context
        );

        // Keep only changes relevant for this reconciliation mode.
        List<HasMetadataChange<C>> filtered = changes
                .getItems()
                .stream()
                .filter(mode::isSupported)
                .collect(Collectors.toList());

        // Execute changes.
        return controller.execute(new DefaultChangeExecutor<>(context, filtered), context);
    }

}
