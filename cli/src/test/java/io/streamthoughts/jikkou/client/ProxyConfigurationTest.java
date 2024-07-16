/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import io.micronaut.context.ApplicationContext;
import io.streamthoughts.jikkou.client.beans.ProxyConfiguration;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProxyConfigurationTest {

    @Test
    void shouldGetProxyConfiguration() {
        // GIVEN
        Map<String, Object> configMap = Map.of("proxy", Map.of(
            "url", "http://localhost:8080",
            "enabled", "true",
            "debugging", "true"
        ));

        ApplicationContext context = ApplicationContext
            .run(new JikkouPropertySource(Configuration.from(configMap)));

        // WHEN
        ProxyConfiguration configuration = context.getBean(ProxyConfiguration.class);

        // THEN
        Assertions.assertNotNull(configuration);
        Assertions.assertEquals("http://localhost:8080", configuration.url());
        Assertions.assertTrue(configuration.enabled());
        Assertions.assertTrue(configuration.debugging());
        //Assertions.assertNull(configuration.basicAuth()); <- Micronaut inject an interceptor
        Assertions.assertTrue(configuration.security().basicAuth().username().isEmpty());
        Assertions.assertTrue(configuration.security().basicAuth().password().isEmpty());
    }

    @Test
    void shouldGetProxyConfigurationGivenBasicAuthProperties() {
        // GIVEN
        Map<String, Object> configMap = Map.of("proxy", Map.of(
            "url", "http://localhost:8080",
            "enabled", "true",
            "debugging", "true",
            "security", Map.of("basic-auth", Map.of(
                "username", "user",
                "password", "pass"
            ))
        ));

        ApplicationContext context = ApplicationContext
            .run(new JikkouPropertySource(Configuration.from(configMap)));

        // WHEN
        ProxyConfiguration configuration = context.getBean(ProxyConfiguration.class);

        // THEN
        Assertions.assertNotNull(configuration);
        Assertions.assertEquals("http://localhost:8080", configuration.url());
        Assertions.assertTrue(configuration.enabled());
        Assertions.assertTrue(configuration.debugging());

        Assertions.assertNotNull(configuration.security().basicAuth());
        ProxyConfiguration.BasicAuth basicAuth = configuration.security().basicAuth();
        Assertions.assertEquals("user", basicAuth.username().get());
        Assertions.assertEquals("pass", basicAuth.password().get());
    }
}
