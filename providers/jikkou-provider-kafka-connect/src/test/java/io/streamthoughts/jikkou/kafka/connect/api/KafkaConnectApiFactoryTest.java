/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaConnectApiFactoryTest {

    private static MockWebServer mockServer;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void afterAll() {
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
                false
        );

        // When
        try (KafkaConnectApi api = KafkaConnectApiFactory.create(config)) {
            api.listConnectors();
        }

        // Then
        String authorization = mockServer.takeRequest().getHeaders().get("Authorization");
        String expectedCredentials = Base64.getEncoder()
                .encodeToString("alice:secret".getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("Basic " + expectedCredentials, authorization,
                "Authorization header must encode the actual credentials, not the Supplier toString()");
    }
}
