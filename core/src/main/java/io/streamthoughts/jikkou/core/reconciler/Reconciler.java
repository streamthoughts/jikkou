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

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Reconcile resources.
 *
 * @param <R> type of the resource.
 */
public final class Reconciler<R extends HasMetadata, C extends ResourceChange> {

    private final Controller<R, C> controller;

    /**
     * Creates a new {@link Reconciler} instance.
     *
     * @param controller the controller instance. Cannot be {@code null}.
     */
    public Reconciler(@NotNull Controller<R, C> controller) {
        this.controller = controller;
    }

    /**
     * Executes all changes for the given reconciliation mode.
     *
     * @param changes The list of changes.
     * @param mode    The reconciliation mode.
     * @param context The reconciliation context.
     * @return The list of results.
     */
    public List<ChangeResult> apply(@NotNull List<C> changes,
                                    @NotNull ReconciliationMode mode,
                                    @NotNull ReconciliationContext context) {
        // Check whether this reconciliation mode is supported.
        checkReconciliationModeIsSupported(mode);

        // Keep only changes relevant for this reconciliation mode.
        List<C> filtered = changes
                .stream()
                .filter(mode::isSupported)
                .collect(Collectors.toList());

        // Execute changes.
        return controller.execute(new DefaultChangeExecutor<>(context, filtered), context);
    }

    private void checkReconciliationModeIsSupported(@NotNull ReconciliationMode mode) {
        if (!isReconciliationModeSupported(mode)) {
            throw new JikkouRuntimeException(
                    String.format(
                            "Cannot execute reconciliation. Mode '%s' is not supported by controller '%s'",
                            mode,
                            controller.getName()
                    )
            );
        }
    }

    private boolean isReconciliationModeSupported(@NotNull ReconciliationMode mode) {
        return Controller.supportedReconciliationModes(controller.getClass()).contains(mode);
    }
}
