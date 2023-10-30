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
