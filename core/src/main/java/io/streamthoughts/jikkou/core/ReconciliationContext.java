/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a reconciliation operation.
 *
 * @see JikkouApi
 */
@InterfaceStability.Evolving
public interface ReconciliationContext {

    /**
     * Returns the {@link Selector} used for restricting the
     * resources that will be included in the current reconciliation operation.
     *
     * @return the {@link Selector}.
     */
    @NotNull Selector selector();

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
    @NotNull NamedValueSet labels();

    /**
     * Returns the 'annotations' to be applied on the metadata of the resources to be reconciled.
     *
     * @return the annotations.
     */
    @NotNull NamedValueSet annotations();

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
                    internal.selector(),
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
                    internal.selector(),
                    internal.configuration(),
                    dryRun,
                    internal.labels(),
                    internal.annotations()
            ));
        }

        /**
         * Returns a new builder with the given selector.
         *
         * @param selector The selector
         * @return a new {@link Builder}
         */
        public Builder selector(Selector selector) {
            return new Builder(new Default(
                    selector,
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
                    internal.selector(),
                    internal.configuration(),
                    internal.isDryRun(),
                    NamedValueSet.setOf(labels),
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
                    internal.selector(),
                    internal.configuration(),
                    internal.isDryRun(),
                    NamedValueSet.setOf(internal.labels()).with(label),
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
                    internal.selector(),
                    internal.configuration(),
                    internal.isDryRun(),
                    internal.labels(),
                    NamedValueSet.setOf(annotations)
            ));
        }

        /**
         * Builds a new {@link ReconciliationContext} instance.
         *
         * @return {@link ReconciliationContext} instance.
         */
        public Builder annotation(NamedValue annotation) {
            return new Builder(new Default(
                    internal.selector(),
                    internal.configuration(),
                    internal.isDryRun(),
                    internal.labels(),
                    NamedValueSet.setOf(internal.annotations()).with(annotation)
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
     * @param selector      The selector to filter resources to be included in the reconciliation.
     * @param configuration The config for computing resource changes.
     * @param isDryRun      Specify if the reconciliation should be run in dry-run.
     */
    record Default(Selector selector,
                   Configuration configuration,
                   boolean isDryRun,
                   NamedValueSet labels,
                   NamedValueSet annotations)
            implements ReconciliationContext {

        public static Default EMPTY = new Default(
                Selectors.NO_SELECTOR,
                Configuration.empty(),
                true,
                NamedValueSet.emptySet(),
                NamedValueSet.emptySet()
        );
    }
}
