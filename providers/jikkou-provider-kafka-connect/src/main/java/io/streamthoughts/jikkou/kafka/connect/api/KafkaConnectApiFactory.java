/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.common.utils.Encoding;
import io.streamthoughts.jikkou.http.client.RestClientBuilder;
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
