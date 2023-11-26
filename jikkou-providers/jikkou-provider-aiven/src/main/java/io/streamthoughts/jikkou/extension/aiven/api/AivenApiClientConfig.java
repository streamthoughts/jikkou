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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;

public final class AivenApiClientConfig {

    public static final String AIVEN_CONFIG_PREFIX = "aiven";

    public static final ConfigProperty<String> AIVEN_PROJECT = ConfigProperty
            .ofString(AIVEN_CONFIG_PREFIX + ".project")
            .description("Aiven project name.");

    public static final ConfigProperty<String> AIVEN_SERVICE = ConfigProperty
            .ofString(AIVEN_CONFIG_PREFIX + ".service")
            .description("Aiven Service name.");

    public static final ConfigProperty<String> AIVEN_API_URL = ConfigProperty
            .ofString(AIVEN_CONFIG_PREFIX + ".apiUrl")
            .orElse("https://api.aiven.io/v1/")
            .description("URL to the Aiven REST API.");

    public static final ConfigProperty<String> AIVEN_TOKEN_AUTH = ConfigProperty
            .ofString(AIVEN_CONFIG_PREFIX + ".tokenAuth")
            .description("Aiven Bearer Token. Tokens can be obtained from your Aiven profile page");

    public static final ConfigProperty<Boolean> AIVEN_DEBUG_LOGGING_ENABLED = ConfigProperty
            .ofBoolean(AIVEN_CONFIG_PREFIX + ".debugLoggingEnabled")
            .description("Enable debug logging.")
            .orElse(false);

    private final Configuration configuration;

    /**
     * Creates a new {@link AivenApiClientConfig} instance.
     *
     * @param configuration the configuration.
     */
    public AivenApiClientConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getApiUrl() {
        return AIVEN_API_URL.get(configuration);
    }

    public String getTokenAuth() {
        return AIVEN_TOKEN_AUTH.get(configuration);
    }

    public String getProject() {
        return AIVEN_PROJECT.get(configuration);
    }

    public String getService() {
        return AIVEN_SERVICE.get(configuration);
    }
    public boolean getDebugLoggingEnabled() {
        return AIVEN_DEBUG_LOGGING_ENABLED.get(configuration);
    }
}
