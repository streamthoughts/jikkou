/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.core.config.Configuration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProxyConfigTest {

    @Test
    void shouldParseAllPropertiesFromConfiguration() {
        Configuration configuration = Configuration.from(Map.of(
                "proxyUrl", "http://proxy.example.com:3128",
                "proxyUsername", "alice",
                "proxyPassword", "secret",
                "nonProxyHosts", "localhost,*.internal"
        ));

        ProxyConfig proxyConfig = ProxyConfig.from(configuration);

        assertEquals("http://proxy.example.com:3128", proxyConfig.proxyUrl());
        assertEquals("alice", proxyConfig.proxyUsername());
        assertEquals("secret", proxyConfig.proxyPassword());
        assertEquals("localhost,*.internal", proxyConfig.nonProxyHosts());
        assertTrue(proxyConfig.hasExplicitProxy());
        assertTrue(proxyConfig.hasCredentials());
    }

    @Test
    void shouldReturnEmptyProxyConfigWhenNoPropertiesSet() {
        ProxyConfig proxyConfig = ProxyConfig.from(Configuration.empty());

        assertNull(proxyConfig.proxyUrl());
        assertNull(proxyConfig.proxyUsername());
        assertNull(proxyConfig.nonProxyHosts());
        assertFalse(proxyConfig.hasExplicitProxy());
        assertFalse(proxyConfig.hasCredentials());
    }

    @Test
    void shouldNotReportCredentialsWhenOnlyUrlSet() {
        ProxyConfig proxyConfig = ProxyConfig.from(
                Configuration.from(Map.of("proxyUrl", "http://proxy.example.com:3128")));

        assertTrue(proxyConfig.hasExplicitProxy());
        assertFalse(proxyConfig.hasCredentials());
    }
}
