/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import io.streamthoughts.jikkou.http.client.ssl.SSLContextFactory;
import io.streamthoughts.jikkou.http.client.ssl.SSLUtils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for creating REST API clients based on JAX-RS annotated interfaces.
 * Uses RESTEasy's native proxy client API.
 */
public class RestClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientBuilder.class);

    public static final AllowAllHostNameVerifier NO_HOST_NAME_VERIFIER = new AllowAllHostNameVerifier();

    private URI baseUri;
    private boolean followRedirects;
    private final Map<String, List<Object>> headers = new HashMap<>();
    private boolean enableClientDebugging = false;
    private final ResteasyClientBuilder clientBuilder;
    private ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

    /**
     * Creates a new {@link RestClientBuilder} instance.
     *
     * @return a new {@link RestClientBuilder} instance.
     */
    public static RestClientBuilder newBuilder() {
        return new RestClientBuilder();
    }

    private RestClientBuilder() {
        this.clientBuilder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
    }

    /**
     * Sets the base URI.
     *
     * @return {@code this}.
     */
    public RestClientBuilder baseUri(String uri) {
        return baseUri(URI.create(uri));
    }

    /**
     * Sets the base URI.
     *
     * @return {@code this}.
     */
    public RestClientBuilder baseUri(URI uri) {
        this.baseUri = uri;
        return this;
    }

    /**
     * Sets the base URL.
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
     * Sets the truststore.
     *
     * @return {@code this}.
     */
    public RestClientBuilder truststore(KeyStore keyStore) {
        clientBuilder.trustStore(keyStore);
        return this;
    }

    /**
     * Sets the keystore.
     *
     * @return {@code this}.
     */
    public RestClientBuilder keystore(KeyStore keyStore, String password) {
        clientBuilder.keyStore(keyStore, password);
        return this;
    }

    public RestClientBuilder sslIgnoreHostnameVerification() {
        clientBuilder.hostnameVerifier(NO_HOST_NAME_VERIFIER);
        return this;
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
        // RESTEasy follows redirects by default; setting is applied via property at build time
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

    public RestClientBuilder sslConfig(final SSLConfig sslConfig) {
        TrustManager[] trustManagers;
        try {
            trustManagers = SSLUtils.createTrustManagers(
                sslConfig.trustStoreLocation(),
                sslConfig.trustStorePassword().toCharArray(),
                sslConfig.trustStoreType(),
                KeyManagerFactory.getDefaultAlgorithm());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            LOG.error("Could not create trust managers for Client Certificate authentication.", e);
            throw new JikkouRuntimeException(e);
        }

        KeyManager[] keyManagers;
        try {
            keyManagers = SSLUtils.createKeyManagers(
                sslConfig.keyStoreLocation(),
                sslConfig.keyStorePassword().toCharArray(),
                sslConfig.keyStoreType(),
                KeyManagerFactory.getDefaultAlgorithm());
        } catch (CertificateException
                 | NoSuchAlgorithmException
                 | UnrecoverableKeyException
                 | KeyStoreException
                 | IOException e) {
            LOG.error("Could not create key managers for Client Certificate authentication.", e);
            throw new JikkouRuntimeException(e);
        }

        SSLContextFactory sslContextFactory = new SSLContextFactory();
        clientBuilder.sslContext(sslContextFactory.getSSLContext(keyManagers, trustManagers));

        return sslConfig.ignoreHostnameVerification() ? sslIgnoreHostnameVerification() : this;
    }

    /**
     * Builds a new client for the given resource interface.
     *
     * @param resourceInterface the interface that defines REST API methods
     * @return a new instance implementing the REST interface
     */
    public <T> T build(Class<T> resourceInterface) {
        if (baseUri == null) {
            throw new IllegalStateException("baseUri has not been set");
        }

        // Register ObjectMapper provider
        clientBuilder.register(new ObjectMapperContextResolver(objectMapper));

        // Register headers filter
        if (!headers.isEmpty()) {
            clientBuilder.register(new HeadersRequestFilter(headers));
        }

        // Register logging filter
        if (enableClientDebugging) {
            clientBuilder.register(new LoggingRequestFilter());
        }

        Client client = clientBuilder.build();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(baseUri);
        target.property("org.jboss.resteasy.follow.redirects", followRedirects);

        return target.proxy(resourceInterface);
    }

    /**
     * ContextResolver that provides a custom ObjectMapper to RESTEasy.
     */
    public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper;

        public ObjectMapperContextResolver(@NotNull ObjectMapper mapper) {
            this.mapper = Objects.requireNonNull(mapper, "objectMapper cannot be null");
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }

    /**
     *
     * ClientRequestFilter that injects custom headers into requests.
     */
    private static class HeadersRequestFilter implements ClientRequestFilter {
        private final Map<String, List<Object>> headers;

        HeadersRequestFilter(Map<String, List<Object>> headers) {
            this.headers = headers;
        }

        @Override
        public void filter(ClientRequestContext requestContext) {
            headers.forEach((name, values) ->
                values.forEach(value -> requestContext.getHeaders().add(name, value)));
        }
    }

    /**
     * ClientRequestFilter that logs HTTP requests.
     */
    private static class LoggingRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) {
            LOG.info("HTTP Request: {} {}", requestContext.getMethod(), requestContext.getUri());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Headers: {}", requestContext.getHeaders());
            }
        }
    }

    /**
     * A {@link HostnameVerifier} that accepts all certificates.
     */
    public static class AllowAllHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(final String hostname, final SSLSession sslSession) {
            return true;
        }
    }
}
