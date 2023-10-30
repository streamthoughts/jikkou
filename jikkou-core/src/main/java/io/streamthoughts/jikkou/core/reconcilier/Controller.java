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
package io.streamthoughts.jikkou.core.reconcilier;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
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
@Enabled
@Category("Controller")
@ControllerConfiguration
public interface Controller<
        R extends HasMetadata,
        C extends Change>
        extends HasMetadataAcceptable, Configurable, Extension {

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

    /**
     * Execute all changes for the given reconciliation mode.
     *
     * @param executor  the executor to be used to applied changed.
     * @param context   the reconciliation context.
     * @return the list of all changes applied.
     */
    List<ChangeResult<C>> execute(@NotNull ChangeExecutor<C> executor,
                                  @NotNull ReconciliationContext context);

    /**
     * Plans all the changes to be executed to reconcile the specified resources.
     *
     * @param resources the list of resources to be reconciled.
     * @param context   the operation context.
     * @return the list of changes to be applied.
     */
    ResourceListObject<? extends HasMetadataChange<C>> plan(@NotNull Collection<R> resources,
                                                            @NotNull ReconciliationContext context);
}
