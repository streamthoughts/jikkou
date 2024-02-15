/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.http.client.security.UsernamePasswordAuthenticator;
import io.streamthoughts.jikkou.http.client.security.UsernamePasswordCredential;
import java.util.Optional;

public final class Config {

    public static final String ENV_SERVICE_HOST = "JIKKOU_SERVICE_HOST";
    public static final String ENV_SERVICE_PORT = "JIKKOU_SERVICE_PORT";
    public static final String DEFAULT_FALLBACK_HOST = "localhost";
    public static final String DEFAULT_FALLBACK_PORT = "8080";

    public static JikkouApiClient fromUrl(final String url) {
        return fromUrl(url, false);
    }

    public static JikkouApiClient fromUrl(final String url,
                                          final boolean validateSSL) {
        ApiClient client = ApiClientBuilder.builder()
                .withBasePath(url)
                .withVerifyingSsl(validateSSL)
                .build();
        return new DefaultJikkouApiClient(client);
    }

    public static JikkouApiClient fromUserPassword(
            final String url, final String user, final String password) {
        return fromUserPassword(url, user, password, true);
    }

    public static JikkouApiClient fromUserPassword(
            final String url, final String user, final String password, final boolean validateSSL) {

        final UsernamePasswordCredential credential = new UsernamePasswordCredential(user, password);
        ApiClient client = ApiClientBuilder.builder()
                .withBasePath(url)
                .withAuthenticator(new UsernamePasswordAuthenticator(() -> credential))
                .withVerifyingSsl(validateSSL)
                .build();
        return new DefaultJikkouApiClient(client);
    }

    /**
     * Creates a default {@link JikkouApiClient} given the following rules:
     *
     * <ul>
     *   <li>If {@code JIKKOU_SERVICE_HOST} environment variable is defined, use that host.
     *   <li>If {@code JIKKOU_SERVICE_PORT} environment variable is defined, use that port.
     *   <li>Otherwise to localhost:8080 as a last resort.
     * </ul>
     *
     * @return a new {@link JikkouApiClient} given the previously described rules.
     */
    public static JikkouApiClient defaultClient() {
        final String serviceHost =
                Optional.ofNullable(System.getenv(ENV_SERVICE_HOST)).orElse(DEFAULT_FALLBACK_HOST);
        final String servicePort =
                Optional.ofNullable(System.getenv(ENV_SERVICE_PORT)).orElse(DEFAULT_FALLBACK_PORT);
        final String basePath = "http://" + serviceHost + ":" + servicePort;
        return fromUrl(basePath);
    }
}