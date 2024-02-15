/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import java.util.Objects;

/**
 * Holds the Jikkou ConfigurationContext.
 */
public final class GlobalConfigurationContext {
    private static ConfigurationContext GLOBAL_CONTEXT;
    // Use to cache loaded configuration.
    private static Configuration CURRENT_CONFIGURATION ;

    /**
     * Gets the current configuration.
     *
     * @return the {@link JikkouConfig}.
     */
    public static synchronized Configuration getConfiguration() {
        mayInitialize();
        return CURRENT_CONFIGURATION;
    }

    /**
     * Sets the global configuration context.
     *
     * @param context   the global context to set.
     */
    public static synchronized void setConfigurationContext(ConfigurationContext context) {
        Objects.requireNonNull(context, "context must not be null");
        GLOBAL_CONTEXT = context;
        CURRENT_CONFIGURATION = context.getCurrentContext().load();
    }

    /**
     * Gets the global configuration context.
     *
     * @return  the {@link ConfigurationContext}.
     */
    public static synchronized ConfigurationContext getConfigurationContext() {
        mayInitialize();
        return GLOBAL_CONTEXT;
    }

    private static synchronized void mayInitialize() {
        if (GLOBAL_CONTEXT == null) {
            setConfigurationContext(ConfigurationContext.createDefaultContext());
        }
    }

    // PRIVATE
    private GlobalConfigurationContext() {}
}
