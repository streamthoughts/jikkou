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
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a reconciliation operation.
 *
 * @see JikkouApi
 */
@InterfaceStability.Evolving
public interface ReconciliationContext {

    /**
     * Returns the {@link ResourceSelector} used for restricting the
     * resources that will be included in the current reconciliation operation.
     *
     * @return the {@link ResourceSelector}.
     */
    @NotNull List<ResourceSelector> selectors();

    /**
     * Returns the {@link Configuration} used for executing a specific resource reconciliation operation.
     *
     * @return the options to be used for computing resource changes.
     */
    @NotNull Configuration configuration();

    /**
     * Returns the 'labels' to be applied on the metadata of the resources to be reconciled.
     *
     * @return the labels.
     */
    @NotNull NamedValue.Set labels();

    /**
     * Returns the 'annotations' to be applied on the metadata of the resources to be reconciled.
     *
     * @return the annotations.
     */
    @NotNull NamedValue.Set annotations();

    /**
     * Checks whether this operation should be run in dry-run.
     *
     * @return {@code true} if the reconciliation operation should be run in dry-mode. Otherwise {@code false}.
     */
    boolean isDryRun();

    /**
     * Gets a new ReconciliationContext builder.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * An immutable class for building a new ReconciliationContext.
     */
    class Builder {

        private final ReconciliationContext internal;

        private Builder() {
            this(Default.EMPTY);
        }

        private Builder(ReconciliationContext context) {
            this.internal = context;
        }

        /**
         * Returns a new builder with the given configuration.
         *
         * @param configuration the configuration
         * @return a new {@link Builder}
         */
        public Builder configuration(Configuration configuration) {
            return new Builder(new Default(
                    internal.selectors(),
                    configuration,
                    internal.isDryRun(),
                    internal.labels(),
                    internal.annotations()
            ));
        }

        /**
         * Returns a new builder with the given dryRun.
         *
         * @param dryRun the selectors
         * @return a new {@link Builder}
         */
        public Builder dryRun(boolean dryRun) {
            return new Builder(new Default(
                    internal.selectors(),
                    internal.configuration(),
                    dryRun,
                    internal.labels(),
                    internal.annotations()
            ));
        }

        /**
         * Returns a new builder with the given selectors.
         *
         * @param selectors the selectors
         * @return a new {@link Builder}
         */
        public Builder selectors(List<ResourceSelector> selectors) {
            return new Builder(new Default(
                    selectors,
                    internal.configuration(),
                    internal.isDryRun(),
                    internal.labels(),
                    internal.annotations()
            ));
        }

        /**
         * Returns a new builder with the given labels.
         *
         * @param labels the labels
         * @return a new {@link Builder}
         */
        public Builder labels(Iterable<NamedValue> labels) {
            return new Builder(new Default(
                    internal.selectors(),
                    internal.configuration(),
                    internal.isDryRun(),
                    NamedValue.setOf(labels),
                    internal.annotations()
            ));
        }

        /**
         * Returns a new builder with the given single label.
         *
         * @param label the labels
         * @return a new {@link Builder}
         */
        public Builder label(NamedValue label) {
            return new Builder(new Default(
                    internal.selectors(),
                    internal.configuration(),
                    internal.isDryRun(),
                    NamedValue.setOf(internal.labels()).with(label),
                    internal.annotations()
            ));
        }

        /**
         * Builds a new {@link ReconciliationContext} instance.
         *
         * @return {@link ReconciliationContext} instance.
         */
        public Builder annotations(Iterable<NamedValue> annotations) {
            return new Builder(new Default(
                    internal.selectors(),
                    internal.configuration(),
                    internal.isDryRun(),
                    internal.labels(),
                    NamedValue.setOf(annotations)
            ));
        }

        /**
         * Builds a new {@link ReconciliationContext} instance.
         *
         * @return {@link ReconciliationContext} instance.
         */
        public Builder annotation(NamedValue annotation) {
            return new Builder(new Default(
                    internal.selectors(),
                    internal.configuration(),
                    internal.isDryRun(),
                    internal.labels(),
                    NamedValue.setOf(internal.annotations()).with(annotation)
            ));
        }

        /**
         * Builds a new {@link ReconciliationContext} instance.
         *
         * @return {@link ReconciliationContext} instance.
         */
        public ReconciliationContext build() {
            return internal;
        }

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
                   boolean isDryRun,
                   NamedValue.Set labels,
                   NamedValue.Set annotations)
            implements ReconciliationContext {

        public static Default EMPTY = new Default(
                Collections.emptyList(),
                Configuration.empty(),
                true,
                NamedValue.emptySet(),
                NamedValue.emptySet()
        );
    }
}
