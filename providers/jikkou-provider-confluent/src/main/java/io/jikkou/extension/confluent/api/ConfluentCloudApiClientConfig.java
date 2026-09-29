/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

import io.jikkou.http.client.proxy.ProxyConfig;
import java.util.Map;

/**
 * Configuration for the Confluent Cloud API client.
 *
 * @param apiUrl               Base URL for the Confluent Cloud API.
 * @param apiKey               Cloud API key (used as HTTP Basic username).
 * @param apiSecret            Cloud API secret (used as HTTP Basic password).
 * @param crnPattern           CRN pattern used to scope role binding list operations.
 * @param proxyConfig          HTTP proxy configuration.
 * @param debugLoggingEnabled  Whether to enable debug logging.
 * @param clientHeaders        Additional HTTP headers to send on every request.
 */
public record ConfluentCloudApiClientConfig(
    String apiUrl,
    String apiKey,
    String apiSecret,
    String crnPattern,
    ProxyConfig proxyConfig,
    boolean debugLoggingEnabled,
    Map<String, String> clientHeaders
) {
}
