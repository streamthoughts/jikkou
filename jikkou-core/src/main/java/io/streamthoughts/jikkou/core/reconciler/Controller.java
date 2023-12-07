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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
public interface Controller<R extends HasMetadata, C extends ResourceChange> extends HasMetadataAcceptable, Extension {

    /**
     * Executes all changes for the given reconciliation mode.
     *
     * @param executor The ChangeExecutor to be used to applied changed.
     * @param context  The ReconciliationContext.
     * @return The list of ChangeResult.
     */
    List<ChangeResult> execute(@NotNull ChangeExecutor<C> executor,
                                  @NotNull ReconciliationContext context);

    /**
     * Plans all the changes to be executed to reconcile the specified resources.
     *
     * @param resources The list of resources to be reconciled.
     * @param context   The ReconciliationContext.
     * @return The list of changes.
     */
    List<C> plan(@NotNull Collection<R> resources,
                 @NotNull ReconciliationContext context);

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
