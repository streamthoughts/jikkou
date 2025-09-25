package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import io.streamthoughts.jikkou.schema.registry.mock.HttpPathBasedDispatcher;
import java.io.IOException;
import java.net.http.HttpHeaders;
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
                String.format("http://%s:%s", schemaRegistryMockServer.getHostName(), schemaRegistryMockServer.getPort()),
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

    @AfterAll
    static void tearDown() {
        schemaRegistryMockServer.close();
    }
}