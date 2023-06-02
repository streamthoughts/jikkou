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
package io.streamthoughts.jikkou.schema.registry.api;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

/**
 * This class is used to abstract the way a REST API is build based on a given interface.
 */
public class RestClientBuilder {

    private URI baseUri;

    private boolean followRedirects;

    private Map<String, List<Object>> headers;

    private final ClientBuilder clientBuilder;

    /**
     * Creates a new {@link RestClientBuilder} instance.
     *
     * @return  a new {@link RestClientBuilder} instance.
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
     * @return  {@code this}.
     */
    public RestClientBuilder baseUri(String uri) {
        return baseUri(URI.create(uri));
    }

    /**
     * Sets the base url.
     *
     * @return  {@code this}.
     */
    public RestClientBuilder baseUri(URI uri) {
        this.baseUri = uri;
        this.headers = new HashMap<>();
        return this;
    }

    /**
     * Sets the base url.
     *
     * @return  {@code this}.
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
     * @return  {@code this}.
     */
    public RestClientBuilder writeTimeout(Duration writeTimeout) {
        clientBuilder.connectTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the read timeout duration.
     * @return  {@code this}.
     */
    public RestClientBuilder readTimeout(Duration readTimeout) {
        clientBuilder.readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS);
        return this;
    }

    public RestClientBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Adds header to request.
     * @param header    the header name.
     * @param value     the header value.
     * @return  {@code this}.
     */
    public RestClientBuilder header(final String header, final Object value) {
        this.headers.computeIfAbsent(header, s -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * Builds a new client for the given resource interface.
     *
     * @param clazz the interface that defines REST API methods for use
     * @return a new instance of an implementation of this REST interface that can be used for making requests to the server.
     */
    public <T> T build(Class<T> clazz) {
        if (baseUri == null) {
            throw new IllegalStateException("baseUri has not been set");
        }

        Client client = clientBuilder
                .register(new ServerExceptionMapper())
                .build();
        WebTarget webTarget = client.target(baseUri);

        webTarget.property(ClientProperties.FOLLOW_REDIRECTS, followRedirects);

        MultivaluedHashMap<String, Object> inboundHeaders = new MultivaluedHashMap<>();
        inboundHeaders.putAll(headers);

        return WebResourceFactory.newResource(
                clazz,
                webTarget,
                false,
                inboundHeaders,
                Collections.emptyList(),
                new Form()
        );
    }

    @Provider
    public static class ServerExceptionMapper implements ExceptionMapper<NotFoundException> {
        @Override
        public Response toResponse(NotFoundException exception) {
            String message = exception.getMessage();
            Response response = exception.getResponse();
            Response.Status status = response.getStatusInfo().toEnum();

            return Response.status(status)
                    .entity(status + ": " + message)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}
