/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.common.utils.Encoding;
import io.streamthoughts.jikkou.http.client.RestClientBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to create new {@link SchemaRegistryApi} instances.
 */
public final class SchemaRegistryApiFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryApiFactory.class);

    /**
     * Creates a new {@link SchemaRegistryApi} for the given configuration.
     *
     * @param config the configuration.
     * @return a new {@link SchemaRegistryApi} instance.
     */
    public static SchemaRegistryApi create(SchemaRegistryClientConfig config) {
        URI baseUri = URI.create(config.getSchemaRegistryUrl());
        LOG.info("Create new Schema Registry client for: {}", baseUri);
        RestClientBuilder builder = RestClientBuilder
                .newBuilder()
                .baseUri(baseUri)
                .enableClientDebugging(config.getDebugLoggingEnabled());

        builder = switch (config.getAuthMethod()) {
            case BASICAUTH -> {
                String buildAuthorizationHeader = getAuthorizationHeader(config);
                builder.header("Authorization", buildAuthorizationHeader);
                yield builder;
            }
            case NONE -> builder;

            case INVALID -> throw new IllegalStateException("Unexpected value: " + config.getAuthMethod());
        };
        return builder.build(SchemaRegistryApi.class);
    }

    @NotNull
    private static String getAuthorizationHeader(SchemaRegistryClientConfig config) {
        String basicAuthInfo = config.getBasicAuthInfo();
        return "Basic " + Encoding.BASE64.encode(basicAuthInfo.getBytes(StandardCharsets.UTF_8));
    }
}
