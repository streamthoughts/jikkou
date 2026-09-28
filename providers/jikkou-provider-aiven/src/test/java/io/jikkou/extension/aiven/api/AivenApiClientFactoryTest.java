/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.api;

import io.jikkou.core.config.Configuration;
import io.jikkou.http.client.proxy.ProxyConfig;
import java.io.IOException;
import java.util.Map;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AivenApiClientFactoryTest {

    private MockWebServer mockServer;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.close();
    }

    @Test
    @DisplayName("Should send the Bearer token when no custom headers are configured")
    void shouldSendBearerTokenByDefault() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("{\"acl\":[]}")
                .build());
        AivenApiClientConfig config = newConfig(Map.of());

        // When
        try (AivenApiClient client = AivenApiClientFactory.create(config)) {
            client.listKafkaAclEntries();
        }

        // Then
        Assertions.assertEquals("Bearer token", mockServer.takeRequest().getHeaders().get("Authorization"));
    }

    @Test
    @DisplayName("Should let custom headers override the built-in Authorization header")
    void shouldLetCustomHeadersOverrideAuthorization() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("{\"acl\":[]}")
                .build());
        AivenApiClientConfig config = newConfig(
                Map.of("Authorization", "Bearer custom-token", "X-Tenant", "acme"));

        // When
        try (AivenApiClient client = AivenApiClientFactory.create(config)) {
            client.listKafkaAclEntries();
        }

        // Then
        var actualHeaders = mockServer.takeRequest().getHeaders();
        Assertions.assertEquals("Bearer custom-token", actualHeaders.get("Authorization"));
        Assertions.assertEquals(1, actualHeaders.values("Authorization").size());
        Assertions.assertEquals("acme", actualHeaders.get("X-Tenant"));
    }

    private AivenApiClientConfig newConfig(Map<String, String> clientHeaders) {
        return new AivenApiClientConfig(
                String.format("http://%s:%s", mockServer.getHostName(), mockServer.getPort()),
                "token",
                "my-project",
                "my-service",
                ProxyConfig.from(Configuration.empty()),
                false,
                clientHeaders
        );
    }
}
