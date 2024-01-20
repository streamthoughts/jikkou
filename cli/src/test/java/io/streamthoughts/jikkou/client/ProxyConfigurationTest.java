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
