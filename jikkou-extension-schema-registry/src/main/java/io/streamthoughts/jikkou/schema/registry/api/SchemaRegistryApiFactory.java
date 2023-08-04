/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.common.utils.Encoding;
import io.streamthoughts.jikkou.rest.client.RestClientBuilder;
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
