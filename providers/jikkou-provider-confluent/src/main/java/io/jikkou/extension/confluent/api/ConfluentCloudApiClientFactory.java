/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

import io.jikkou.http.client.RestClientBuilder;
import java.net.URI;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link ConfluentCloudApiClient} instances.
 */
public class ConfluentCloudApiClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ConfluentCloudApiClientFactory.class);

    /**
     * Creates a new {@link ConfluentCloudApiClient} for the given configuration.
     *
     * @param config the configuration.
     * @return a new {@link ConfluentCloudApiClient} instance.
     */
    public static ConfluentCloudApiClient create(ConfluentCloudApiClientConfig config) {
        URI baseUri = URI.create(config.apiUrl());
        LOG.info(
            "Create new REST client for Confluent Cloud API: {} (debugLoggingEnabled: {})",
            baseUri,
            config.debugLoggingEnabled()
        );
        String credentials = Base64.getEncoder().encodeToString(
            (config.apiKey() + ":" + config.apiSecret()).getBytes()
        );
        RestClientBuilder builder = RestClientBuilder
            .newBuilder()
            .enableClientDebugging(config.debugLoggingEnabled())
            .baseUri(baseUri);

        builder.header("Authorization", "Basic " + credentials);
        return new ConfluentCloudApiClient(
            builder.build(ConfluentCloudApi.class),
            config.crnPattern()
        );
    }
}
