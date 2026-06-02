/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.http.client.proxy.ProxyConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Base64;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestClientBuilderProxyTest {

    private MockWebServer proxy;

    @BeforeEach
    void setUp() throws IOException {
        proxy = new MockWebServer();
        proxy.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        proxy.shutdown();
    }

    @Test
    void shouldRouteRequestThroughExplicitProxy() throws Exception {
        proxy.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(200)
                .setBody("pong"));

        ProxyConfig proxyConfig = new ProxyConfig(proxy.url("/").toString(), null, null, null);

        Ping resource = RestClientBuilder.newBuilder()
                .baseUri("http://dummy-target.invalid/")
                .proxyConfig(proxyConfig)
                .build(Ping.class);

        assertEquals("pong", resource.ping());

        RecordedRequest request = proxy.takeRequest();
        // A forward proxy receives the absolute request URI in the request line.
        assertTrue(request.getPath().startsWith("http://dummy-target.invalid"),
                "expected absolute-form request target, got: " + request.getPath());
    }

    @Test
    void shouldSendProxyAuthorizationAfterChallenge() throws Exception {
        proxy.enqueue(new MockResponse()
                .setResponseCode(407)
                .setHeader("Proxy-Authenticate", "Basic realm=\"proxy\""));
        proxy.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(200)
                .setBody("pong"));

        ProxyConfig proxyConfig = new ProxyConfig(proxy.url("/").toString(), "alice", "secret", null);

        Ping resource = RestClientBuilder.newBuilder()
                .baseUri("http://dummy-target.invalid/")
                .proxyConfig(proxyConfig)
                .build(Ping.class);

        assertEquals("pong", resource.ping());

        RecordedRequest first = proxy.takeRequest();   // no credentials yet
        RecordedRequest second = proxy.takeRequest();  // retried with credentials
        String expected = "Basic " + Base64.getEncoder()
                .encodeToString("alice:secret".getBytes());
        assertEquals(expected, second.getHeader("Proxy-Authorization"));
    }

    @Test
    void shouldBypassProxyForNonProxyHosts() {
        // Proxy points to an unreachable address; nonProxyHosts must force a direct
        // connection to the MockWebServer, otherwise the request would fail.
        proxy.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(200)
                .setBody("pong"));

        ProxyConfig proxyConfig = new ProxyConfig(
                "http://bogus-proxy.invalid:3128", null, null, "localhost,127.0.0.1");

        Ping resource = RestClientBuilder.newBuilder()
                .baseUri(proxy.url("/").toString())
                .proxyConfig(proxyConfig)
                .build(Ping.class);

        assertEquals("pong", resource.ping());
    }

    @Test
    void shouldHonorSystemPropertyProxyAsFallback() throws Exception {
        proxy.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(200)
                .setBody("pong"));

        System.setProperty("http.proxyHost", proxy.getHostName());
        System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
        try {
            ProxyConfig emptyProxyConfig = new ProxyConfig(null, null, null, null);

            Ping resource = RestClientBuilder.newBuilder()
                    .baseUri("http://dummy-target.invalid/")
                    .proxyConfig(emptyProxyConfig)
                    .build(Ping.class);

            assertEquals("pong", resource.ping());

            RecordedRequest request = proxy.takeRequest();
            assertTrue(request.getPath().startsWith("http://dummy-target.invalid"),
                    "expected request routed via system-property proxy, got: " + request.getPath());
        } finally {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
        }
    }

    @Path("/")
    public interface Ping {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("ping")
        String ping();
    }
}
