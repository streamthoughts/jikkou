/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.SslConfigSupport;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import java.util.Objects;

public class KafkaConnectClientConfig {

    public static final ConfigProperty<String> KAFKA_CONNECT_NAME = ConfigProperty
        .ofString("name")
        .description("Name of the kafka connect cluster.");

    public static final ConfigProperty<String> KAFKA_CONNECT_URL = ConfigProperty
        .ofString("url")
        .description("URL to establish connection to kafka connect cluster.");

    public static final ConfigProperty<String> KAFKA_CONNECT_AUTH_METHOD = ConfigProperty
        .ofString("authMethod")
        .orElse(AuthMethod.NONE.name())
        .description("Method to use for authenticating on Kafka Connect cluster. Available values are: [none, basicauth, ssl]");

    public static final ConfigProperty<String> KAFKA_CONNECT_BASIC_AUTH_USERNAME = ConfigProperty
        .ofString("basicAuthUser")
        .description("Use when 'kafkaConnect.authMethod' is 'basicauth' to specify the username for Authorization Basic header");

    public static final ConfigProperty<String> KAFKA_CONNECT_BASIC_AUTH_PASSWORD = ConfigProperty
        .ofString("basicAuthPassword")
        .description("Use when 'kafkaConnect.authMethod' is 'basicauth' to specify the password for Authorization Basic header");

    public static final ConfigProperty<Boolean> KAFKA_CONNECT_DEBUG_LOGGING_ENABLED = ConfigProperty
        .ofBoolean("debugLoggingEnabled")
        .description("Enable debug logging.")
        .orElse(false);

    private final Configuration configuration;

    /**
     * Creates a new {@link KafkaConnectClientConfig} instance.
     *
     * @param configuration the configuration.
     */
    public KafkaConnectClientConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getConnectClusterName() {
        return KAFKA_CONNECT_NAME.get(configuration);
    }

    public String getConnectUrl() {
        return KAFKA_CONNECT_URL.get(configuration);
    }

    public AuthMethod getAuthMethod() {
        return Enums.getForNameIgnoreCase(KAFKA_CONNECT_AUTH_METHOD.get(configuration), AuthMethod.class, AuthMethod.INVALID);
    }

    public String getBasicAuthUsername() {
        return KAFKA_CONNECT_BASIC_AUTH_USERNAME.get(configuration);
    }

    public String getBasicAuthPassword() {
        return KAFKA_CONNECT_BASIC_AUTH_PASSWORD.get(configuration);
    }

    public String getBasicAuthInfo() {
        return getBasicAuthUsername() + ":" + getBasicAuthPassword();
    }

    public boolean getDebugLoggingEnabled() {
        return KAFKA_CONNECT_DEBUG_LOGGING_ENABLED.get(configuration);
    }

    public SSLConfig getSslConfig() {
        return SslConfigSupport.getSslConfig(null, configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaConnectClientConfig that = (KafkaConnectClientConfig) o;
        return Objects.equals(configuration, that.configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(configuration);
    }

}
