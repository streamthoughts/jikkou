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
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("integration")
public class AbstractAivenIntegrationTest {

    static MockWebServer SERVER;

    static AivenApiClientConfig AIVEN_API_CONFIG;

    @BeforeAll
    public static void beforeAll() throws IOException {
        SERVER = new MockWebServer();
        SERVER.start();
        Configuration configuration = new Configuration
                .Builder()
                .with(AivenApiClientConfig.AIVEN_API_URL.key(), SERVER.url("/"))
                .with(AivenApiClientConfig.AIVEN_PROJECT.key(), "project")
                .with(AivenApiClientConfig.AIVEN_SERVICE.key(), "service")
                .with(AivenApiClientConfig.AIVEN_TOKEN_AUTH.key(), "token")
                .with(AivenApiClientConfig.AIVEN_DEBUG_LOGGING_ENABLED.key(), true)
                .build();
        AIVEN_API_CONFIG = new AivenApiClientConfig(configuration);
    }

    @AfterAll
    static void tearDown() throws IOException {
        SERVER.shutdown();
    }

    public static AivenApiClientConfig getAivenApiConfig() {
        return AIVEN_API_CONFIG;
    }

    public static void enqueueResponse(MockResponse response) {
        SERVER.enqueue(response);
    }
}
