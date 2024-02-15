/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RestClientBuilderTest {

    public static MockWebServer SERVER;

    @BeforeAll
    static void setUp() throws IOException {
        SERVER = new MockWebServer();
        SERVER.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        SERVER.shutdown();
    }

    @Test
    void shouldGenerateRestClientForJaxRsInterface() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong")
        );

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(SERVER.url("/").toString())
                .enableClientDebugging(true)
                .build(TestResource.class);

        // Then
        try (resource) {
            String response = resource.ping();
            Assertions.assertEquals("pong", response);
        }
    }


    @Path("/")
    interface TestResource extends AutoCloseable {

        @GET()
        @Produces("application/json")
        @Path("ping")
        String ping();

        @Override
        default void close() {}
    }
}