/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.ssl;

/**
 * SSL Configs.
 *
 * @param keyStoreLocation
 * @param keyStorePassword
 * @param keyStoreType
 * @param keyPassword
 * @param trustStoreLocation
 * @param trustStorePassword
 * @param trustStoreType
 */
public record SSLConfig(
    String keyStoreLocation,
    String keyStorePassword,
    String keyStoreType,
    String keyPassword,
    String trustStoreLocation,
    String trustStorePassword,
    String trustStoreType,
    boolean ignoreHostnameVerification
) {
}