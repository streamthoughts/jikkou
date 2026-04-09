/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.reconciler;

import io.jikkou.common.annotation.InterfaceStability.Evolving;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.ReconciliationMode;
import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.extension.Extension;
import io.jikkou.core.extension.ExtensionCategory;
import io.jikkou.core.extension.annotations.Category;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasMetadataAcceptable;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for computing and applying the changes required to reconcile resources.
 * A controller implementation can only reconcile resources of the same type.
 *
 * @param <R> type of the resource managed by this controller.
 */
@Evolving
@Enabled
@Category(ExtensionCategory.CONTROLLER)
@ControllerConfiguration
public interface Controller<R extends HasMetadata> extends HasMetadataAcceptable, Extension {

    /**
     * Executes all changes for the given reconciliation mode.
     *
     * @param executor The ChangeExecutor to be used to applied changed.
     * @param context  The ReconciliationContext.
     * @return The list of ChangeResult.
     */
    List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                               @NotNull ReconciliationContext context);

    /**
     * Plans all the changes to be executed to reconcile the specified resources.
     *
     * @param resources The list of resources to be reconciled.
     * @param context   The ReconciliationContext.
     * @return The list of changes.
     */
    List<ResourceChange> plan(@NotNull Collection<R> resources,
                              @NotNull ReconciliationContext context);

    /**
     * Enriches actual resources with labels from name-matched expected resources.
     * Labels from expected resources are propagated to actual resources (joined by name),
     * preserving any existing labels on the actual resources (e.g., system labels).
     * This is used to make label selectors work correctly: actual (collected) resources
     * lack user-defined labels, so they must be enriched before the selector is applied.
     *
     * @param actual   the list of actual (collected) resources.
     * @param expected the list of expected (input) resources.
     * @param <R>      the resource type.
     */
    static <R extends HasMetadata> void enrichLabelsFromExpected(
            @NotNull List<R> actual,
            @NotNull List<R> expected) {

        Map<String, Map<String, Object>> labelsByName = expected.stream()
                .collect(Collectors.toMap(
                        t -> t.getMetadata().getName(),
                        t -> t.getMetadata().getLabels(),
                        (a, b) -> b
                ));

        for (R resource : actual) {
            Map<String, Object> expectedLabels = labelsByName.get(resource.getMetadata().getName());
            if (expectedLabels == null || expectedLabels.isEmpty()) {
                continue;
            }
            expectedLabels.forEach(resource.getMetadata()::addLabelIfAbsent);
        }
    }

    /**
     * Gets the set of reconciliation modes supported by this controller.
     *
     * @param clazz the extension clazz.
     * @return the set of reconciliation modes.
     */
    static Set<ReconciliationMode> supportedReconciliationModes(Class<? extends Extension> clazz) {
        ControllerConfiguration configuration = clazz.getAnnotation(ControllerConfiguration.class);
        if (configuration != null) {
            return Set.of(configuration.supportedModes());
        }
        return Collections.emptySet();
    }
}
