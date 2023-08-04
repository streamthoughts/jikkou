/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.rest.client;

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