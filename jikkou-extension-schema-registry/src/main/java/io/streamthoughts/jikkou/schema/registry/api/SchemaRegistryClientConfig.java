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
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;

public class SchemaRegistryClientConfig {

    public static final String SCHEMA_REGISTRY_CONFIG_PREFIX = "schemaRegistry";

    public static final ConfigProperty<String> SCHEMA_REGISTRY_URL = ConfigProperty
            .ofString(SCHEMA_REGISTRY_CONFIG_PREFIX + ".url")
            .description("Comma-separated list of URLs for schema registry instances that can be used to register or look up schemas.");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_VENDOR_NAME = ConfigProperty
            .ofString(SCHEMA_REGISTRY_CONFIG_PREFIX + ".vendor")
            .orElse("generic")
            .description("The name of the schema registry implementation vendor.");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_AUTH_METHOD = ConfigProperty
            .ofString(SCHEMA_REGISTRY_CONFIG_PREFIX + ".authMethod")
            .orElse(AuthMethod.NONE.name())
            .description("Method to use for authenticating on Schema Registry. Available values are: [none, basicauth]");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_USERNAME = ConfigProperty
            .ofString(SCHEMA_REGISTRY_CONFIG_PREFIX + ".basicAuthUser")
            .description("Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the username for Authorization Basic header");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD = ConfigProperty
            .ofString(SCHEMA_REGISTRY_CONFIG_PREFIX + ".basicAuthPassword")
            .description("Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the password for Authorization Basic header");

    public static final ConfigProperty<Boolean> SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED = ConfigProperty
            .ofBoolean(SCHEMA_REGISTRY_CONFIG_PREFIX + ".debugLoggingEnabled")
            .description("Enable debug logging.")
            .orElse(false);


    private final Configuration configuration;

    /**
     * Creates a new {@link SchemaRegistryClientConfig} instance.
     *
     * @param configuration the configuration.
     */
    public SchemaRegistryClientConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getSchemaRegistryUrl() {
        return SCHEMA_REGISTRY_URL.evaluate(configuration);
    }

    public String getSchemaRegistryVendor() {
        return SCHEMA_REGISTRY_VENDOR_NAME.evaluate(configuration);
    }

    public AuthMethod getAuthMethod() {
        return AuthMethod.getForNameIgnoreCase(SCHEMA_REGISTRY_AUTH_METHOD.evaluate(configuration));
    }

    public String getBasicAuthUsername() {
        return SCHEMA_REGISTRY_BASIC_AUTH_USERNAME.evaluate(configuration);
    }

    public String getBasicAuthPassword() {
        return SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD.evaluate(configuration);
    }

    public String getBasicAuthInfo() {
        return getBasicAuthUsername() + ":" + getBasicAuthPassword();
    }

    public boolean getDebugLoggingEnabled() {
        return SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED.evaluate(configuration);
    }
}
