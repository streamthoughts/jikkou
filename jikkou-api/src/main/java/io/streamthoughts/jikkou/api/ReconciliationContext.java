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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a reconciliation operation.
 *
 * @see ResourceController
 */
@InterfaceStability.Evolving
public interface ReconciliationContext {

    /**
     * Returns the {@link ResourceSelector} used for restricting the
     * resources that should be included in the current operation.
     *
     * @return the {@link ResourceSelector}.
     */
    @NotNull List<ResourceSelector> selectors();

    /**
     * Returns the {@link Configuration} used for executing
     * a specific resource reconciliation operation.
     *
     * @return the options to be used for computing resource changes.
     */
    @NotNull Configuration configuration();

    /**
     * Checks whether this operation should be run in dry-run.
     *
     * @return {@code true} if the update operation should be run in dry-mode. Otherwise {@code false}.
     */
    boolean isDryRun();

    /**
     * Helper method to create a new {@link ReconciliationContext} for the given arguments.
     *
     * @param isDryRun      specify if the update should be run in dry-run.
     * @return a new {@link ReconciliationContext}
     */
    @NotNull
    static ReconciliationContext with(final boolean isDryRun) {
        return with(Collections.emptyList(), Configuration.empty(), isDryRun);
    }

    /**
     * Helper method to create a new {@link ReconciliationContext} for the given arguments.
     *
     * @param configuration the options for computing resource changes.
     * @param isDryRun      specify if the update should be run in dry-run.
     * @return a new {@link ReconciliationContext}
     */
    @NotNull
    static ReconciliationContext with(@NotNull final Configuration configuration,
                                      final boolean isDryRun) {
        return with(Collections.emptyList(), configuration, isDryRun);
    }

    /**
     * Helper method to create a new {@link ReconciliationContext} for the given arguments.
     *
     * @param selectors the selectors to filter resources to be included in the reconciliation.
     * @param config    the config for computing resource changes.
     * @param isDryRun  specify if the reconciliation should be run in dry-run.
     * @return a new {@link ReconciliationContext}
     */
    @NotNull
    static ReconciliationContext with(@NotNull final List<ResourceSelector> selectors,
                                      @NotNull final Configuration config,
                                      final boolean isDryRun) {
        return new Default(selectors, config, isDryRun);
    }

    /**
     * A default {@link ReconciliationContext} implementation.
     *
     * @param selectors     the selectors to filter resources to be included in the reconciliation.
     * @param configuration the config for computing resource changes.
     * @param isDryRun      specify if the reconciliation should be run in dry-run.
     */
    record Default(List<ResourceSelector> selectors,
                   Configuration configuration,
                   boolean isDryRun)
            implements ReconciliationContext {
    }
}
