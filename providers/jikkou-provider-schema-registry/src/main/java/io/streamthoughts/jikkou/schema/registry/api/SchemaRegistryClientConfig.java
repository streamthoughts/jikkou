/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.SslConfigSupport;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;

public class SchemaRegistryClientConfig {

    public static final String CONFIG_NAMESPACE = "schemaRegistry";

    public static final ConfigProperty<String> SCHEMA_REGISTRY_URL = ConfigProperty
        .ofString(CONFIG_NAMESPACE + ".url")
        .description("Comma-separated list of URLs for schema registry instances that can be used to register or look up schemas.");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_VENDOR_NAME = ConfigProperty
        .ofString(CONFIG_NAMESPACE + ".vendor")
        .orElse("generic")
        .description("The name of the schema registry implementation vendor.");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_AUTH_METHOD = ConfigProperty
        .ofString(CONFIG_NAMESPACE + ".authMethod")
        .orElse(AuthMethod.NONE.name())
        .description("Method to use for authenticating on Schema Registry. Available values are: [none, basicauth, ssl]");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_USERNAME = ConfigProperty
        .ofString(CONFIG_NAMESPACE + ".basicAuthUser")
        .description("Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the username for Authorization Basic header");

    public static final ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD = ConfigProperty
        .ofString(CONFIG_NAMESPACE + ".basicAuthPassword")
        .description("Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the password for Authorization Basic header");

    public static final ConfigProperty<Boolean> SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED = ConfigProperty
        .ofBoolean(CONFIG_NAMESPACE + ".debugLoggingEnabled")
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
        return SCHEMA_REGISTRY_URL.get(configuration);
    }

    public String getSchemaRegistryVendor() {
        return SCHEMA_REGISTRY_VENDOR_NAME.get(configuration);
    }

    public AuthMethod getAuthMethod() {
        return Enums.getForNameIgnoreCase(SCHEMA_REGISTRY_AUTH_METHOD.get(configuration), AuthMethod.class, AuthMethod.INVALID);
    }

    public String getBasicAuthUsername() {
        return SCHEMA_REGISTRY_BASIC_AUTH_USERNAME.get(configuration);
    }

    public String getBasicAuthPassword() {
        return SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD.get(configuration);
    }

    public String getBasicAuthInfo() {
        return getBasicAuthUsername() + ":" + getBasicAuthPassword();
    }

    public boolean getDebugLoggingEnabled() {
        return SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED.get(configuration);
    }

    public SSLConfig getSslConfig() {
        return SslConfigSupport.getSslConfig(CONFIG_NAMESPACE, configuration);
    }
}
