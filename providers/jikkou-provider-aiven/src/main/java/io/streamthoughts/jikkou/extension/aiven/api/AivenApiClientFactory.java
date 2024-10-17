/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.http.client.RestClientBuilder;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AivenApiClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AivenApiClientFactory.class);

    /**
     * Creates a new {@link AivenApiClientConfig} for the given configuration.
     *
     * @param config the configuration.
     * @return a new {@link AivenApiClientConfig} instance.
     */
    public static AivenApiClient create(AivenApiClientConfig config) {
        URI baseUri = URI.create(config.apiUrl());
        LOG.info(
                "Create new REST client for Aiven API: {} (debugLoggingEnabled: {})",
                baseUri,
                config.debugLoggingEnabled()
        );
        RestClientBuilder builder = RestClientBuilder
                .newBuilder()
                .enableClientDebugging(config.debugLoggingEnabled())
                .baseUri(baseUri);

        builder.header("Authorization", "Bearer " + config.tokenAuth());
        return new AivenApiClient(
                builder.build(AivenApi.class),
                config.project(),
                config.service()
        );
    }
}
