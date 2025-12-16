/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
public final class Reconciler<R extends HasMetadata> {

    private final Controller<R> controller;

    /**
     * Creates a new {@link Reconciler} instance.
     *
     * @param controller the controller instance. Cannot be {@code null}.
     */
    public Reconciler(@NotNull Controller<R> controller) {
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
    public List<ChangeResult> apply(@NotNull List<ResourceChange> changes,
                                    @NotNull ReconciliationMode mode,
                                    @NotNull ReconciliationContext context) {
        // Check whether this reconciliation mode is supported.
        checkReconciliationModeIsSupported(mode);

        // Keep only changes relevant for this reconciliation mode.
        List<ResourceChange> filtered = changes
                .stream()
                .filter(mode::isSupported)
                .collect(Collectors.toList());

        // Execute changes.
        return controller.execute(new DefaultChangeExecutor(context, filtered), context);
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
