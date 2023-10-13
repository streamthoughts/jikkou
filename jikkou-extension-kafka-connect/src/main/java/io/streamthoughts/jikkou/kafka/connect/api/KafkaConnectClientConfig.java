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
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
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
            .description("Method to use for authenticating on Kafka Connect cluster. Available values are: [none, basicauth]");

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
        return KAFKA_CONNECT_NAME.evaluate(configuration);
    }

    public String getConnectUrl() {
        return KAFKA_CONNECT_URL.evaluate(configuration);
    }

    public AuthMethod getAuthMethod() {
        return AuthMethod.getForNameIgnoreCase(KAFKA_CONNECT_AUTH_METHOD.evaluate(configuration));
    }

    public String getBasicAuthUsername() {
        return KAFKA_CONNECT_BASIC_AUTH_USERNAME.evaluate(configuration);
    }

    public String getBasicAuthPassword() {
        return KAFKA_CONNECT_BASIC_AUTH_PASSWORD.evaluate(configuration);
    }

    public String getBasicAuthInfo() {
        return getBasicAuthUsername() + ":" + getBasicAuthPassword();
    }

    public boolean getDebugLoggingEnabled() {
        return KAFKA_CONNECT_DEBUG_LOGGING_ENABLED.evaluate(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaConnectClientConfig that = (KafkaConnectClientConfig) o;
        return Objects.equals(configuration, that.configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(configuration);
    }
}
