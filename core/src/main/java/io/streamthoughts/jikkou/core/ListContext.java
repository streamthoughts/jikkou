/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a list operation.
 *
 * @see JikkouApi
 */
@InterfaceStability.Evolving
public interface ListContext {

    /**
     * Returns the {@link Selector} used for filtering the resources.
     *
     * @return the {@link Selector}.
     */
    @NotNull Selector selector();

    /**
     * Returns the {@link Configuration} used for executing a specific resource list operation.
     *
     * @return the options to be used for listing resources.
     */
    @NotNull Configuration configuration();

    /**
     * Returns the selected provider name for this list operation.
     *
     * @return the provider name, or null if not specified.
     */
    String providerName();

    /**
     * Gets a new ListContext builder.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * An immutable class for building a new ListContext.
     */
    class Builder {

        private final ListContext internal;

        private Builder() {
            this(Default.EMPTY);
        }

        private Builder(ListContext context) {
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
                    internal.providerName()
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
                    internal.providerName()
            ));
        }

        /**
         * Returns a new builder with the given provider name.
         *
         * @param providerName the provider name
         * @return a new {@link Builder}
         */
        public Builder providerName(String providerName) {
            return new Builder(new Default(
                    internal.selector(),
                    internal.configuration(),
                    providerName
            ));
        }

        /**
         * Builds a new {@link ListContext} instance.
         *
         * @return {@link ListContext} instance.
         */
        public ListContext build() {
            return internal;
        }
    }

    /**
     * A default {@link ListContext} implementation.
     *
     * @param selector      The selector to filter resources.
     * @param configuration The configuration for listing resources.
     * @param providerName  The selected provider name for this list operation.
     */
    record Default(Selector selector,
                   Configuration configuration,
                   String providerName)
            implements ListContext {

        public static Default EMPTY = new Default(
                Selectors.NO_SELECTOR,
                Configuration.empty(),
                null
        );
    }
}