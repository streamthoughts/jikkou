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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.annotation.ExtensionType;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptable;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to compute and apply changes required to reconcile resources into a managed system.
 *
 * @param <R> type of the resource managed by this controller.
 * @param <C> type of the change managed by this controller.
 */
@Evolving
@ExtensionType("Controller")
public interface ResourceController<
        R extends HasMetadata,
        C extends Change>
        extends HasMetadataAcceptable, Extension, AutoCloseable {

    /**
     * Gets the set of reconciliation modes supported by this controller.
     *
     * @param clazz the extension clazz.
     * @return the set of reconciliation modes.
     */
    static Set<ReconciliationMode> supportedReconciliationModes(Class<? extends Extension> clazz) {
        AcceptsReconciliationModes supported = clazz.getAnnotation(AcceptsReconciliationModes.class);
        if (supported != null) {
            return Set.of(supported.value());
        }
        return Collections.emptySet();
    }

    /**
     * Gets the set of reconciliation modes supported by this controller.
     *
     * @return the set of reconciliation modes.
     */
    default Set<ReconciliationMode> supportedReconciliationModes() {
        return supportedReconciliationModes(getClass());
    }

    /**
     * Executes the reconciliation of all the given resources.
     *
     * @param resources the list of resources to be reconciled.
     * @param mode      the reconciliation mode to be executed.
     * @param context   the context of the reconciliation.
     * @return the list of changes
     */
    List<ChangeResult<C>> reconcile(@NotNull final List<R> resources,
                                    @NotNull final ReconciliationMode mode,
                                    @NotNull final ReconciliationContext context);

    /**
     * Computes all the changes to be applied to reconcile the given resources.
     *
     * @param resources the list of resources to be reconciled.
     * @param mode      the reconciliation mode.
     * @param context   the operation context.
     * @return the list of changes to be applied.
     */
    ResourceListObject<? extends HasMetadataChange<C>> computeReconciliationChanges(@NotNull Collection<R> resources,
                                                                                    @NotNull ReconciliationMode mode,
                                                                                    @NotNull ReconciliationContext context);

    /**
     * Closes any I/O resources.
     */
    @Override
    default void close() {
    }

}
