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

import io.streamthoughts.jikkou.common.utils.Encoding;
import io.streamthoughts.jikkou.rest.client.RestClientBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to create new {@link KafkaConnectApi} instances.
 */
public final class KafkaConnectApiFactory {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectApiFactory.class);

    /**
     * Creates a new {@link KafkaConnectApi} for the given configuration.
     *
     * @param config the configuration.
     * @return a new {@link KafkaConnectApi} instance.
     */
    public static KafkaConnectApi create(KafkaConnectClientConfig config) {
        return create(config, null);
    }

    /**
     * Creates a new {@link KafkaConnectApi} for the given configuration.
     *
     * @param config  the configuration.
     * @param timeout the read/write timeout.
     * @return a new {@link KafkaConnectApi} instance.
     */
    public static KafkaConnectApi create(@NotNull KafkaConnectClientConfig config,
                                         @Nullable Duration timeout) {
        URI baseUri = URI.create(config.getConnectUrl());
        LOG.info("Create new Kafka Connect client for: {}", baseUri);
        RestClientBuilder builder = RestClientBuilder
                .newBuilder()
                .baseUri(baseUri)
                .enableClientDebugging(config.getDebugLoggingEnabled());

        if (timeout != null) {
            builder.writeTimeout(timeout)
                    .readTimeout(timeout);
        }

        builder = switch (config.getAuthMethod()) {
            case BASICAUTH -> {
                String buildAuthorizationHeader = getAuthorizationHeader(config);
                builder.header("Authorization", buildAuthorizationHeader);
                yield builder;
            }
            case NONE -> builder;

            case INVALID -> throw new IllegalStateException("Unexpected value: " + config.getAuthMethod());
        };
        return builder.build(KafkaConnectApi.class);
    }

    @NotNull
    private static String getAuthorizationHeader(KafkaConnectClientConfig config) {
        String basicAuthInfo = config.getBasicAuthInfo();
        return "Basic " + Encoding.BASE64.encode(basicAuthInfo.getBytes(StandardCharsets.UTF_8));
    }
}
