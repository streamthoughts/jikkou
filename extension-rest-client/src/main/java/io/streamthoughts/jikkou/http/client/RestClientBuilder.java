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
package io.streamthoughts.jikkou.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.http.client.internal.ProxyInvocationHandler;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.jetbrains.annotations.NotNull;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This class is used to abstract the way a REST API is build based on a given interface.
 */
public class RestClientBuilder {

    private URI baseUri;

    private boolean followRedirects;

    private Map<String, List<Object>> headers;

    private boolean enableClientDebugging = false;

    private final ClientBuilder clientBuilder;

    private ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

    /**
     * Creates a new {@link RestClientBuilder} instance.
     *
     * @return a new {@link RestClientBuilder} instance.
     */
    public static RestClientBuilder newBuilder() {
        return new RestClientBuilder();
    }

    /**
     * Creates a new {@link RestClientBuilder} instance.
     */
    private RestClientBuilder() {
        this.clientBuilder = ClientBuilder.newBuilder();
    }

    /**
     * Sets the base url.
     *
     * @return {@code this}.
     */
    public RestClientBuilder baseUri(String uri) {
        return baseUri(URI.create(uri));
    }

    /**
     * Sets the base url.
     *
     * @return {@code this}.
     */
    public RestClientBuilder baseUri(URI uri) {
        this.baseUri = uri;
        this.headers = new HashMap<>();
        return this;
    }

    /**
     * Sets the base url.
     *
     * @return {@code this}.
     */
    public RestClientBuilder baseUrl(URL url) {
        try {
            this.baseUri = url.toURI();
            return this;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Sets the connect timeout.
     *
     * @return {@code this}.
     */
    public RestClientBuilder writeTimeout(Duration writeTimeout) {
        clientBuilder.connectTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the read timeout duration.
     * @return {@code this}.
     */
    public RestClientBuilder readTimeout(Duration readTimeout) {
        clientBuilder.readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS);
        return this;
    }

    public RestClientBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public RestClientBuilder enableClientDebugging(boolean enableClientDebugging) {
        this.enableClientDebugging = enableClientDebugging;
        return this;
    }

    /**
     * Adds HTTP header to request.
     *
     * @param header the header name.
     * @param value  the header value.
     * @return {@code this}.
     */
    public RestClientBuilder header(final String header, final Object value) {
        this.headers.computeIfAbsent(header, s -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * Adds HTTP headers to request.
     *
     * @param headers the HTTP headers
     * @return {@code this}.
     */
    public RestClientBuilder headers(final Map<String, Object> headers) {
        headers.forEach(this::header);
        return this;
    }

    /**
     * Sets a custom {@link ObjectMapper} to be used for
     * serializing and deserializing HTTP request/response entity.
     *
     * @param objectMapper the {@link ObjectMapper}.
     * @return {@code this}.
     */
    public RestClientBuilder objectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Builds a new client for the given resource interface.
     *
     * @param resourceInterface the interface that defines REST API methods for use
     * @return a new instance of an implementation of this REST interface that can be used for making requests to the server.
     */
    public <T> T build(Class<T> resourceInterface) {
        if (baseUri == null) {
            throw new IllegalStateException("baseUri has not been set");
        }

        ClientBuilder cb = clientBuilder;
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        LoggingFeature.LoggingFeatureBuilder builder = LoggingFeature.builder()
                .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME));

        if (enableClientDebugging) {
            builder = builder
                    .level(Level.INFO)
                    .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY);
        }
        cb = cb.register(builder.build());

        Client client = cb
                .register(new CustomJacksonMapperProvider(objectMapper))
                .register(new JacksonJsonProvider())
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .build();

        WebTarget webTarget = client.target(baseUri);
        webTarget.property(ClientProperties.FOLLOW_REDIRECTS, followRedirects);

        MultivaluedHashMap<String, Object> inboundHeaders = new MultivaluedHashMap<>();
        inboundHeaders.putAll(headers);

        return ProxyInvocationHandler.newResource(
                resourceInterface,
                client,
                webTarget,
                inboundHeaders
        );
    }

    @Provider
    public static class CustomJacksonMapperProvider implements ContextResolver<ObjectMapper> {

        private final ObjectMapper mapper;

        public CustomJacksonMapperProvider(final @NotNull ObjectMapper mapper) {
            this.mapper = Objects.requireNonNull(mapper, "objectMapper cannot be null");
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }
}
