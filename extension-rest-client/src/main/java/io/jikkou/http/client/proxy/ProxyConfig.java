/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client.proxy;

import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;

/**
 * HTTP proxy configuration for REST-based providers.
 *
 * @param proxyUrl       the proxy URL, e.g. {@code http://proxy.example.com:3128}.
 * @param proxyUsername  the username for proxy Basic authentication (optional).
 * @param proxyPassword  the password for proxy Basic authentication (optional).
 * @param nonProxyHosts  comma-separated list of hosts that bypass the proxy (optional).
 */
public record ProxyConfig(
    String proxyUrl,
    String proxyUsername,
    String proxyPassword,
    String nonProxyHosts
) {

    public static final ConfigProperty<String> PROXY_URL = ConfigProperty
        .ofString("proxyUrl")
        .description("HTTP proxy URL used to reach the target API, e.g. 'http://proxy.example.com:3128'. "
            + "When empty, standard JVM proxy system properties (http.proxyHost / https.proxyHost) are honored as a fallback.");

    public static final ConfigProperty<String> PROXY_USERNAME = ConfigProperty
        .ofString("proxyUsername")
        .description("Username for proxy Basic authentication. Optional.");

    public static final ConfigProperty<String> PROXY_PASSWORD = ConfigProperty
        .ofString("proxyPassword")
        .description("Password for proxy Basic authentication. Optional.");

    public static final ConfigProperty<String> NON_PROXY_HOSTS = ConfigProperty
        .ofString("nonProxyHosts")
        .description("Comma-separated list of hosts that should bypass the proxy. "
            + "Supports leading or trailing '*' wildcards, e.g. 'localhost,127.0.0.1,*.internal'.");

    /**
     * Creates a {@link ProxyConfig} from the given configuration.
     *
     * @param configuration the configuration.
     * @return a new {@link ProxyConfig}.
     */
    public static ProxyConfig from(final Configuration configuration) {
        return new ProxyConfig(
            PROXY_URL.getOptional(configuration).orElse(null),
            PROXY_USERNAME.getOptional(configuration).orElse(null),
            PROXY_PASSWORD.getOptional(configuration).orElse(null),
            NON_PROXY_HOSTS.getOptional(configuration).orElse(null)
        );
    }

    /**
     * @return {@code true} if an explicit proxy URL has been configured.
     */
    public boolean hasExplicitProxy() {
        return proxyUrl != null && !proxyUrl.isBlank();
    }

    /**
     * @return {@code true} if proxy Basic-auth credentials have been configured.
     */
    public boolean hasCredentials() {
        return proxyUsername != null && !proxyUsername.isBlank();
    }
}
