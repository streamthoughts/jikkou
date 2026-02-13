/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import java.util.List;
import java.util.function.Supplier;

public record SchemaRegistryClientConfig(
    List<String> urls,
    String vendor,
    AuthMethod authMethod,
    Supplier<String> basicAuthUser,
    Supplier<String> basicAuthPassword,
    Supplier<SSLConfig> sslConfig,
    Boolean debugLoggingEnabled
) {

    /**
     * Returns the first URL from the list.
     *
     * @return the first configured URL.
     */
    public String firstUrl() {
        return urls.getFirst();
    }
}
