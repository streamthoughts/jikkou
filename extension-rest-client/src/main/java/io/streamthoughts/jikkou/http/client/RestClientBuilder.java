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
import io.streamthoughts.jikkou.http.client.internal.ProxyInvocationHandler;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import io.streamthoughts.jikkou.http.client.ssl.SSLContextFactory;
import io.streamthoughts.jikkou.http.client.ssl.SSLUtils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This class is used to abstract the way a REST API is build based on a given interface.
 */
public class RestClientBuilder {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RestClientBuilder.class);

    public static final AllowAllHostNameVerifier NO_HOST_NAME_VERIFIER = new AllowAllHostNameVerifier();

    private URI baseUri;

    private boolean followRedirects;

    private Map<String, List<Object>> headers;

    private boolean enableClientDebugging = false;

    private final ClientBuilder clientBuilder;

    private SSLContext sslContext;

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
                KeyManagerFactory.getDefaultAlgorithm()
            );
        } catch (CertificateException |
                 NoSuchAlgorithmException |
                 KeyStoreException |
                 IOException e) {
            LOG.error("Could not create trust managers for Client Certificate authentication.", e);
            throw new JikkouRuntimeException(e);
        }
        KeyManager[] keyManagers;
        try {
            keyManagers = SSLUtils.createKeyManagers(
                sslConfig.keyStoreLocation(),
                sslConfig.keyStorePassword().toCharArray(),
                sslConfig.keyStoreType(),
                KeyManagerFactory.getDefaultAlgorithm()
            );
        } catch (CertificateException |
                 NoSuchAlgorithmException |
                 UnrecoverableKeyException |
                 KeyStoreException |
                 IOException e) {
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

    /**
     * A {@link HostnameVerifier} that accept all certificates.
     */
    public static class AllowAllHostNameVerifier implements HostnameVerifier {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean verify(final String hostname, final SSLSession sslSession) {
            return true;
        }
    }

}
