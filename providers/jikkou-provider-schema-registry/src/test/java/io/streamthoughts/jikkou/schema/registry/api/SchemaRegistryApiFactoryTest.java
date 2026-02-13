/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import io.streamthoughts.jikkou.schema.registry.mock.HttpPathBasedDispatcher;
import java.io.IOException;
import java.util.List;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemaRegistryApiFactoryTest {

    private static MockWebServer schemaRegistryMockServer = new MockWebServer();

    @BeforeAll
    static void beforeAll() throws IOException {
        schemaRegistryMockServer = new MockWebServer();
        schemaRegistryMockServer.start();
    }

    @Test
    @DisplayName("Should build authorization header with basic auth")
    public void getAuthorizationHeader() throws InterruptedException {
        // Given
        HttpPathBasedDispatcher schemaRegistryHTTPDispatcher = HttpPathBasedDispatcher.builder()
                .forPath("/subjects/subject-value/versions", new MockResponse.Builder()
                        .code(200)
                        .addHeader("Content-Type", "application/vnd.schemaregistry.v1+json")
                        .body("[]")
                        .build())
                .build();
        schemaRegistryMockServer.setDispatcher(schemaRegistryHTTPDispatcher);
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                List.of(String.format("http://%s:%s", schemaRegistryMockServer.getHostName(), schemaRegistryMockServer.getPort())),
                "generic",
                AuthMethod.BASICAUTH,
                () -> "username",
                () -> "password",
                () -> SSLConfig.from(Configuration.empty()),
                false
        );

        // When
        List<Integer> versions;
        try (SchemaRegistryApi schemaRegistryApi = SchemaRegistryApiFactory.create(config)) {
            versions = schemaRegistryApi.getAllSubjectVersions("subject-value");
        }

        // Then
        Assertions.assertNotNull(versions);
        String authorization = schemaRegistryMockServer.takeRequest().getHeaders().get("Authorization");
        // result should correspond to base64 encoded string "username:password" prefixed with "Basic"
        Assertions.assertEquals(authorization, "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
    }

    @Test
    @DisplayName("Should failover to second URL when first server is down")
    public void shouldFailoverToSecondUrl() throws IOException {
        // Given
        MockWebServer downServer = new MockWebServer();
        downServer.start();
        int downPort = downServer.getPort();
        downServer.close(); // shut it down to simulate unreachable server

        MockWebServer upServer = new MockWebServer();
        upServer.start();
        HttpPathBasedDispatcher dispatcher = HttpPathBasedDispatcher.builder()
                .forPath("/subjects", new MockResponse.Builder()
                        .code(200)
                        .addHeader("Content-Type", "application/vnd.schemaregistry.v1+json")
                        .body("[\"subject-1\",\"subject-2\"]")
                        .build())
                .build();
        upServer.setDispatcher(dispatcher);

        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                List.of(
                        String.format("http://localhost:%s", downPort),
                        String.format("http://%s:%s", upServer.getHostName(), upServer.getPort())
                ),
                "generic",
                AuthMethod.NONE,
                () -> null,
                () -> null,
                () -> SSLConfig.from(Configuration.empty()),
                false
        );

        // When
        List<String> subjects;
        try (SchemaRegistryApi schemaRegistryApi = SchemaRegistryApiFactory.create(config)) {
            subjects = schemaRegistryApi.listSubjects();
        }

        // Then
        Assertions.assertNotNull(subjects);
        Assertions.assertEquals(List.of("subject-1", "subject-2"), subjects);

        upServer.close();
    }

    @Test
    @DisplayName("Should create single client when only one URL is configured")
    public void shouldCreateSingleClientForOneUrl() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                List.of(String.format("http://%s:%s", schemaRegistryMockServer.getHostName(), schemaRegistryMockServer.getPort())),
                "generic",
                AuthMethod.NONE,
                () -> null,
                () -> null,
                () -> SSLConfig.from(Configuration.empty()),
                false
        );

        // When
        try (SchemaRegistryApi api = SchemaRegistryApiFactory.create(config)) {
            // Then - should not be a FailoverSchemaRegistryApi
            Assertions.assertFalse(api instanceof FailoverSchemaRegistryApi);
        }
    }

    @Test
    @DisplayName("Should create failover client when multiple URLs are configured")
    public void shouldCreateFailoverClientForMultipleUrls() {
        // Given
        SchemaRegistryClientConfig config = new SchemaRegistryClientConfig(
                List.of("http://host-a:8081", "http://host-b:8081"),
                "generic",
                AuthMethod.NONE,
                () -> null,
                () -> null,
                () -> SSLConfig.from(Configuration.empty()),
                false
        );

        // When
        try (SchemaRegistryApi api = SchemaRegistryApiFactory.create(config)) {
            // Then - should be a FailoverSchemaRegistryApi
            Assertions.assertInstanceOf(FailoverSchemaRegistryApi.class, api);
        }
    }

    @AfterAll
    static void tearDown() {
        schemaRegistryMockServer.close();
    }
}
