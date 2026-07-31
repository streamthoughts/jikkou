/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.http.client.ssl.SSLConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RestClientBuilderTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldGenerateRestClientForJaxRsInterface() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = newClient(TestResource.class);
        String response = resource.ping();

        // Then
        assertEquals("pong", response);
    }

    @Test
    void shouldSendPostRequestWithJsonBody() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("{\"id\":42,\"name\":\"created\"}"));

        // When
        TestResource resource = newClient(TestResource.class);
        Item result = resource.createItem(new Item(0, "test"));

        // Then
        assertEquals(42, result.id());
        assertEquals("created", result.name());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/items", request.getPath());
        assertEquals("application/json", request.getHeader("Content-Type"));
    }

    @Test
    void shouldSendPutRequestWithPathParam() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"updated\"}"));

        // When
        TestResource resource = newClient(TestResource.class);
        Item result = resource.updateItem(1, new Item(1, "updated"));

        // Then
        assertEquals("updated", result.name());

        RecordedRequest request = server.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/items/1", request.getPath());
    }

    @Test
    void shouldSendDeleteRequestWithPathParam() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse().setResponseCode(204));

        // When
        TestResource resource = newClient(TestResource.class);
        resource.deleteItem(99);

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/items/99", request.getPath());
    }

    @Test
    void shouldSendQueryParameters() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("[]"));

        // When
        TestResource resource = newClient(TestResource.class);
        resource.searchItems("test", 10);

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/items/search?q=test&limit=10", request.getPath());
    }

    @Test
    void shouldSendDefaultValueForQueryParam() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("[]"));

        // When
        TestResource resource = newClient(TestResource.class);
        resource.listItems(null);

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertNotNull(request.getPath());
    }

    @Test
    void shouldSendCustomHeaders() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .header("Authorization", "Bearer test-token")
                .header("X-Custom-Header", "custom-value")
                .build(TestResource.class);
        resource.ping();

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer test-token", request.getHeader("Authorization"));
        assertEquals("custom-value", request.getHeader("X-Custom-Header"));
    }

    @Test
    void shouldReplaceHeaderValueWhenUsingSetHeader() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .header("Authorization", "Basic first")
                .setHeader("Authorization", "Bearer second")
                .build(TestResource.class);
        resource.ping();

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer second", request.getHeader("Authorization"));
        assertEquals(1, request.getHeaders().values("Authorization").size());
    }

    @Test
    void shouldTreatHeaderNamesAsCaseInsensitive() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .header("Authorization", "Basic first")
                .setHeader("authorization", "Bearer second")
                .build(TestResource.class);
        resource.ping();

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals(1, request.getHeaders().values("Authorization").size());
        assertEquals("Bearer second", request.getHeader("Authorization"));
    }

    @Test
    void shouldLetClientHeadersOverrideBuiltInAuthorizationHeader() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .header("Authorization", "Basic built-in")
                .clientHeaders(Map.of("Authorization", "Bearer custom", "X-Tenant", "acme"))
                .build(TestResource.class);
        resource.ping();

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals(1, request.getHeaders().values("Authorization").size());
        assertEquals("Bearer custom", request.getHeader("Authorization"));
        assertEquals("acme", request.getHeader("X-Tenant"));
    }

    @Test
    void shouldIgnoreNullAndEmptyClientHeaders() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .clientHeaders(null)
                .clientHeaders(Map.of())
                .build(TestResource.class);

        // Then
        assertEquals("pong", resource.ping());
    }

    @Test
    void shouldKeepAppendSemanticsForHeaderMethod() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .header("X-Multi", "one")
                .header("X-Multi", "two")
                .build(TestResource.class);
        resource.ping();

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals(2, request.getHeaders().values("X-Multi").size());
    }

    @Test
    void shouldFlagWellKnownSensitiveHeaderNames() {
        assertTrue(RestClientBuilder.isSensitiveHeader("Authorization"));
        assertTrue(RestClientBuilder.isSensitiveHeader("authorization"));
        assertTrue(RestClientBuilder.isSensitiveHeader("Proxy-Authorization"));
        assertTrue(RestClientBuilder.isSensitiveHeader("Cookie"));
        assertTrue(RestClientBuilder.isSensitiveHeader("Set-Cookie"));
    }

    @Test
    void shouldFlagHeaderNamesMatchingSecretPatterns() {
        assertTrue(RestClientBuilder.isSensitiveHeader("X-API-Key"));
        assertTrue(RestClientBuilder.isSensitiveHeader("X-Auth-Token"));
        assertTrue(RestClientBuilder.isSensitiveHeader("X-Client-Secret"));
        assertTrue(RestClientBuilder.isSensitiveHeader("X-Password"));
    }

    @Test
    void shouldNotFlagOrdinaryHeaderNames() {
        assertFalse(RestClientBuilder.isSensitiveHeader("Content-Type"));
        assertFalse(RestClientBuilder.isSensitiveHeader("Accept"));
        assertFalse(RestClientBuilder.isSensitiveHeader("X-Tenant"));
        assertFalse(RestClientBuilder.isSensitiveHeader(null));
    }

    @Test
    void shouldMaskSensitiveHeaderValuesAndKeepOthersReadable() {
        // Given
        Map<String, List<Object>> headers = new LinkedHashMap<>();
        headers.put("Content-Type", List.of("application/json"));
        headers.put("Authorization", List.of("Basic dXNlcjpwYXNz"));
        headers.put("X-Api-Key", List.of("super-secret"));
        headers.put("X-Tenant", List.of("acme"));

        // When
        String redacted = RestClientBuilder.redactHeaders(headers);

        // Then
        assertEquals(
                "{Content-Type=[application/json], Authorization=***, X-Api-Key=***, X-Tenant=[acme]}",
                redacted);
    }

    @Test
    void shouldDeserializeJsonListResponse() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]"));

        // When
        TestResource resource = newClient(TestResource.class);
        List<Item> items = resource.listItems(10);

        // Then
        assertEquals(2, items.size());
        assertEquals(1, items.getFirst().id());
        assertEquals("b", items.get(1).name());
    }

    @Test
    void shouldThrowWebApplicationExceptionOnHttpError() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"error\":\"not found\"}"));

        // When/Then
        TestResource resource = newClient(TestResource.class);
        WebApplicationException exception =
                assertThrows(WebApplicationException.class, () -> resource.getItem(999));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void shouldThrowWebApplicationExceptionOnServerError() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"error\":\"internal error\"}"));

        // When/Then
        TestResource resource = newClient(TestResource.class);
        WebApplicationException exception =
                assertThrows(WebApplicationException.class, resource::ping);
        assertEquals(500, exception.getResponse().getStatus());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBaseUriNotSet() {
        assertThrows(IllegalStateException.class, () ->
                RestClientBuilder.newBuilder().build(TestResource.class));
    }

    @Test
    void shouldSupportCustomMediaTypes() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/vnd.custom.v1+json")
                .setResponseCode(200)
                .setBody("[\"subject1\",\"subject2\"]"));

        // When
        CustomMediaTypeResource resource = newClient(CustomMediaTypeResource.class);
        List<String> subjects = resource.listSubjects();

        // Then
        assertEquals(2, subjects.size());
        assertEquals("subject1", subjects.getFirst());

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/subjects", request.getPath());
    }

    @Test
    void shouldSupportInterfaceLevelPath() throws InterruptedException {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("{\"status\":\"ok\"}"));

        // When
        NestedPathResource resource = newClient(NestedPathResource.class);
        resource.getStatus("my-project", "my-service");

        // Then
        RecordedRequest request = server.takeRequest();
        assertEquals("/project/my-project/service/my-service/status", request.getPath());
    }

    @Test
    void shouldAcceptEmptySslConfigWithoutThrowing() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));
        SSLConfig sslConfig = SSLConfig.from(Configuration.empty());

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .sslConfig(sslConfig)
                .build(TestResource.class);
        String response = resource.ping();

        // Then
        assertEquals("pong", response);
    }

    @Test
    void shouldAcceptSslConfigWithOnlyHostnameVerificationDisabledWithoutThrowing() {
        // Given
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));
        SSLConfig sslConfig = new SSLConfig(
                null, null, "PKCS12", null,
                null, null, "PKCS12",
                true);

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .sslConfig(sslConfig)
                .build(TestResource.class);
        String response = resource.ping();

        // Then
        assertEquals("pong", response);
    }

    @Test
    void shouldAcceptSslConfigWithOnlyTrustStoreWithoutThrowing(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Given
        java.nio.file.Path truststorePath = tempDir.resolve("truststore.jks");
        char[] password = "changeit".toCharArray();
        KeyStore truststore = KeyStore.getInstance("JKS");
        truststore.load(null, password);
        try (OutputStream os = Files.newOutputStream(truststorePath)) {
            truststore.store(os, password);
        }
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("pong"));
        SSLConfig sslConfig = new SSLConfig(
                null, null, "PKCS12", null,
                truststorePath.toString(), "changeit", "JKS",
                false);

        // When
        TestResource resource = RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .sslConfig(sslConfig)
                .build(TestResource.class);
        String response = resource.ping();

        // Then
        assertEquals("pong", response);
    }

    private <T> T newClient(Class<T> resourceInterface) {
        return RestClientBuilder.newBuilder()
                .baseUri(server.url("/").toString())
                .enableClientDebugging(true)
                .build(resourceInterface);
    }

    // -- Test interfaces and DTOs

    public record Item(@JsonProperty("id") int id, @JsonProperty("name") String name) {}

    @Path("/")
    public interface TestResource {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("ping")
        String ping();

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("items/{id}")
        Item getItem(@PathParam("id") int id);

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("items")
        Item createItem(Item item);

        @PUT
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("items/{id}")
        Item updateItem(@PathParam("id") int id, Item item);

        @DELETE
        @Path("items/{id}")
        void deleteItem(@PathParam("id") int id);

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("items/search")
        List<Item> searchItems(@QueryParam("q") String query, @QueryParam("limit") int limit);

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("items")
        List<Item> listItems(@DefaultValue("100") @QueryParam("limit") Integer limit);
    }

    @Path("/")
    public interface CustomMediaTypeResource {

        @GET
        @Path("subjects")
        @Produces("application/vnd.custom.v1+json")
        List<String> listSubjects();
    }

    @Path("/project/{project}/service/{service}")
    @Produces(MediaType.APPLICATION_JSON)
    public interface NestedPathResource {

        @GET
        @Path("status")
        String getStatus(@PathParam("project") String project, @PathParam("service") String service);
    }
}