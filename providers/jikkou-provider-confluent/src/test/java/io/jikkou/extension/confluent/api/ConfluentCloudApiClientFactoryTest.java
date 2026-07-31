/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

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

class ConfluentCloudApiClientFactoryTest {

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
    @DisplayName("Should send the basic Authorization header when no custom headers are configured")
    void shouldSendBasicAuthorizationByDefault() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("{}")
                .build());
        ConfluentCloudApiClientConfig config = newConfig(Map.of());

        // When
        try (ConfluentCloudApiClient client = ConfluentCloudApiClientFactory.create(config)) {
            client.getRoleBinding("rb-123");
        }

        // Then
        String authorization = mockServer.takeRequest().getHeaders().get("Authorization");
        Assertions.assertNotNull(authorization);
        Assertions.assertTrue(authorization.startsWith("Basic "));
    }

    @Test
    @DisplayName("Should let custom headers override the built-in Authorization header")
    void shouldLetCustomHeadersOverrideAuthorization() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("{}")
                .build());
        ConfluentCloudApiClientConfig config = newConfig(
                Map.of("Authorization", "Bearer custom-token", "X-Tenant", "acme"));

        // When
        try (ConfluentCloudApiClient client = ConfluentCloudApiClientFactory.create(config)) {
            client.getRoleBinding("rb-123");
        }

        // Then
        var actualHeaders = mockServer.takeRequest().getHeaders();
        Assertions.assertEquals("Bearer custom-token", actualHeaders.get("Authorization"));
        Assertions.assertEquals(1, actualHeaders.values("Authorization").size());
        Assertions.assertEquals("acme", actualHeaders.get("X-Tenant"));
    }

    private ConfluentCloudApiClientConfig newConfig(Map<String, String> clientHeaders) {
        return new ConfluentCloudApiClientConfig(
                String.format("http://%s:%s", mockServer.getHostName(), mockServer.getPort()),
                "api-key",
                "api-secret",
                "crn://confluent.cloud/kafka=lkc-000000",
                ProxyConfig.from(Configuration.empty()),
                false,
                clientHeaders
        );
    }
}
