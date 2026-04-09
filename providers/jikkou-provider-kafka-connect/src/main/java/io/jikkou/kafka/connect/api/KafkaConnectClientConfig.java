/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.api;

import io.jikkou.common.utils.Enums;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.http.client.ssl.SSLConfig;
import java.util.function.Supplier;

public record KafkaConnectClientConfig(
    String name,
    String url,
    AuthMethod authMethod,
    Supplier<String> basicAuthUser,
    Supplier<String> basicAuthPassword,
    Supplier<SSLConfig> sslConfig,
    Boolean debugLoggingEnabled
) {

    public static final ConfigProperty<String> KAFKA_CONNECT_NAME = ConfigProperty
        .ofString("name")
        .displayName("Cluster Name")
        .description("Name of the kafka connect cluster.");

    public static final ConfigProperty<String> KAFKA_CONNECT_URL = ConfigProperty
        .ofString("url")
        .displayName("URL")
        .description("URL to establish connection to kafka connect cluster.");

    public static final ConfigProperty<String> KAFKA_CONNECT_AUTH_METHOD = ConfigProperty
        .ofString("authMethod")
        .displayName("Auth Method")
        .defaultValue(AuthMethod.NONE.name())
        .description("Method to use for authenticating on Kafka Connect cluster. Available values are: [none, basicauth, ssl]");

    public static final ConfigProperty<String> KAFKA_CONNECT_BASIC_AUTH_USERNAME = ConfigProperty
        .ofString("basicAuthUser")
        .displayName("Basic Auth Username")
        .description("Use when 'kafkaConnect.authMethod' is 'basicauth' to specify the username for Authorization Basic header");

    public static final ConfigProperty<String> KAFKA_CONNECT_BASIC_AUTH_PASSWORD = ConfigProperty
        .ofString("basicAuthPassword")
        .displayName("Basic Auth Password")
        .description("Use when 'kafkaConnect.authMethod' is 'basicauth' to specify the password for Authorization Basic header");

    public static final ConfigProperty<Boolean> KAFKA_CONNECT_DEBUG_LOGGING_ENABLED = ConfigProperty
        .ofBoolean("debugLoggingEnabled")
        .displayName("Debug Logging")
        .description("Enable debug logging.")
        .defaultValue(false);

    public static KafkaConnectClientConfig from(final Configuration configuration) {
        return new KafkaConnectClientConfig(
            KAFKA_CONNECT_NAME.get(configuration),
            KAFKA_CONNECT_URL.get(configuration),
            Enums.getForNameIgnoreCase(KAFKA_CONNECT_AUTH_METHOD.get(configuration), AuthMethod.class, AuthMethod.INVALID),
            () -> KAFKA_CONNECT_BASIC_AUTH_USERNAME.get(configuration),
            () -> KAFKA_CONNECT_BASIC_AUTH_PASSWORD.get(configuration),
            () -> SSLConfig.from(configuration),
            KAFKA_CONNECT_DEBUG_LOGGING_ENABLED.get(configuration)
        );
    }
}
