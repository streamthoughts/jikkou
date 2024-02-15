/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import org.jetbrains.annotations.NotNull;

/**
 * Any class that need be configured with external config properties should implement that interface.
 *
 * @see Configuration
 */
@InterfaceStability.Evolving
public interface Configurable {

    /**
     * Configures this class with the given configuration.
     *
     * @param config    the {@link Configuration}.
     */
    default void configure(@NotNull Configuration config) throws ConfigException {
        // intentionally left blank
    }
}