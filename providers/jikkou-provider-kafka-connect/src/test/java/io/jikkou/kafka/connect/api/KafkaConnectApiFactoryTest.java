/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.api;

import io.jikkou.core.config.Configuration;
import io.jikkou.http.client.proxy.ProxyConfig;
import io.jikkou.http.client.ssl.SSLConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaConnectApiFactoryTest {

    private static MockWebServer mockServer = new MockWebServer();

    @BeforeAll
    static void beforeAll() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockServer.close();
    }

    @Test
    @DisplayName("Should build Authorization header from actual basicAuth credentials")
    void shouldBuildBasicAuthHeaderFromActualCredentials() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("[]")
                .build());
        KafkaConnectClientConfig config = new KafkaConnectClientConfig(
                "test-cluster",
                String.format("http://%s:%s", mockServer.getHostName(), mockServer.getPort()),
                AuthMethod.BASICAUTH,
                () -> "alice",
                () -> "secret",
                () -> SSLConfig.from(Configuration.empty()),
                () -> ProxyConfig.from(Configuration.empty()),
                false,
                Map.of()
        );

        // When
        try (KafkaConnectApi api = KafkaConnectApiFactory.create(config)) {
            api.listConnectors();
        }

        // Then
        String authorization = mockServer.takeRequest().getHeaders().get("Authorization");
        String expectedCredentials = Base64.getEncoder()
                .encodeToString("alice:secret".getBytes(StandardCharsets.UTF_8));
        // result should correspond to base64 encoded string "alice:secret" prefixed with "Basic"
        Assertions.assertEquals("Basic " + expectedCredentials, authorization,
                "Authorization header must contain the base64-encoded credentials");
    }

    @Test
    @DisplayName("Should let a custom Authorization header override basicauth")
    void shouldLetCustomAuthorizationHeaderOverrideBasicAuth() throws InterruptedException {
        // Given
        mockServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body("[]")
                .build());
        KafkaConnectClientConfig config = new KafkaConnectClientConfig(
                "test-cluster",
                String.format("http://%s:%s", mockServer.getHostName(), mockServer.getPort()),
                AuthMethod.BASICAUTH,
                () -> "alice",
                () -> "secret",
                () -> SSLConfig.from(Configuration.empty()),
                () -> ProxyConfig.from(Configuration.empty()),
                false,
                Map.of("Authorization", "Bearer custom-token", "X-Tenant", "acme")
        );

        // When
        try (KafkaConnectApi api = KafkaConnectApiFactory.create(config)) {
            api.listConnectors();
        }

        // Then
        var actualHeaders = mockServer.takeRequest().getHeaders();
        Assertions.assertEquals("Bearer custom-token", actualHeaders.get("Authorization"));
        Assertions.assertEquals(1, actualHeaders.values("Authorization").size());
        Assertions.assertEquals("acme", actualHeaders.get("X-Tenant"));
    }

    @Test
    @DisplayName("Should read clientHeaders from configuration")
    void shouldReadClientHeadersFromConfiguration() {
        // Given
        Configuration configuration = Configuration.from(Map.of(
                "name", "test-cluster",
                "url", "http://localhost:8083",
                "clientHeaders", Map.of("X-Tenant", "acme")
        ));

        // When
        KafkaConnectClientConfig config = KafkaConnectClientConfig.from(configuration);

        // Then
        Assertions.assertEquals(Map.of("X-Tenant", "acme"), config.clientHeaders());
    }
}
