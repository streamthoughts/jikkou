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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.rest.client.RestClientBuilder;
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
        URI baseUri = URI.create(config.getApiUrl());
        LOG.info(
                "Create new REST client for Aiven API: {} (debugLoggingEnabled: {})",
                baseUri,
                config.getDebugLoggingEnabled()
        );
        RestClientBuilder builder = RestClientBuilder
                .newBuilder()
                .enableClientDebugging(config.getDebugLoggingEnabled())
                .baseUri(baseUri);

        builder.header("Authorization", "Bearer " + config.getTokenAuth());
        return new AivenApiClient(
                builder.build(AivenApi.class),
                config.getProject(),
                config.getService()
        );
    }
}
