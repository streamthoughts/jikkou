/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

/**
 * Configuration for the Confluent Cloud API client.
 *
 * @param apiUrl               Base URL for the Confluent Cloud API.
 * @param apiKey               Cloud API key (used as HTTP Basic username).
 * @param apiSecret            Cloud API secret (used as HTTP Basic password).
 * @param crnPattern           CRN pattern used to scope role binding list operations.
 * @param debugLoggingEnabled  Whether to enable debug logging.
 */
public record ConfluentCloudApiClientConfig(
    String apiUrl,
    String apiKey,
    String apiSecret,
    String crnPattern,
    boolean debugLoggingEnabled
) {
}
