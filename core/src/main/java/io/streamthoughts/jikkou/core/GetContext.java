/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.config.Configuration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a get resource operation.
 *
 * @see JikkouApi
 */
@InterfaceStability.Evolving
public interface GetContext {

    /**
     * Returns the {@link Configuration} used for executing a specific resource get operation.
     *
     * @return the options to be used for getting a resource.
     */
    @NotNull Configuration configuration();

    /**
     * Returns the selected provider name for this get operation.
     *
     * @return the provider name, or null if not specified.
     */
    String providerName();

    /**
     * Gets a new GetContext builder.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * An immutable class for building a new GetContext.
     */
    class Builder {

        private final GetContext internal;

        private Builder() {
            this(Default.EMPTY);
        }

        private Builder(GetContext context) {
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
                    configuration,
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
                    internal.configuration(),
                    providerName
            ));
        }

        /**
         * Builds a new {@link GetContext} instance.
         *
         * @return {@link GetContext} instance.
         */
        public GetContext build() {
            return internal;
        }
    }

    /**
     * A default {@link GetContext} implementation.
     *
     * @param configuration The configuration for getting a resource.
     * @param providerName  The selected provider name for this get operation.
     */
    record Default(Configuration configuration,
                   String providerName)
            implements GetContext {

        public static Default EMPTY = new Default(
                Configuration.empty(),
                null
        );
    }
}
