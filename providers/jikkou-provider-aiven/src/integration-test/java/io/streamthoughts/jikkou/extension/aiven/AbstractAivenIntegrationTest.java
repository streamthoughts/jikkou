/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("integration")
public class AbstractAivenIntegrationTest {

    static MockWebServer SERVER;

    static AivenApiClientConfig AIVEN_API_CONFIG;

    @BeforeEach
    public void beforeAll() throws IOException {
        SERVER = new MockWebServer();
        SERVER.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        SERVER.shutdown();
    }

    public static AivenApiClientConfig getAivenApiConfig() {
        return AIVEN_API_CONFIG;
    }

    public static void enqueueResponse(MockResponse response) {
        SERVER.enqueue(response);
    }
}
