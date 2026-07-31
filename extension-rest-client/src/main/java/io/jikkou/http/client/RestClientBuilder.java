/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.io.Jackson;
import io.jikkou.http.client.proxy.ProxyConfig;
import io.jikkou.http.client.ssl.SSLConfig;
import io.jikkou.http.client.ssl.SSLContextFactory;
import io.jikkou.http.client.ssl.SSLUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
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
    private final Map<String, List<Object>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean enableClientDebugging = false;
    private final ResteasyClientBuilder clientBuilder;
    private ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;
    private SSLContext sslContext;
    private boolean ignoreHostnameVerification;
    private Duration connectTimeout;
    private Duration readTimeout;
    private ProxyConfig proxyConfig;

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
        this.ignoreHostnameVerification = true;
        clientBuilder.hostnameVerifier(NO_HOST_NAME_VERIFIER);
        return this;
    }

    /**
     * Sets the connect timeout.
     *
     * @return {@code this}.
     */
    public RestClientBuilder writeTimeout(Duration writeTimeout) {
        this.connectTimeout = writeTimeout;
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
        this.readTimeout = readTimeout;
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
     * Sets an HTTP header on the request, replacing any value previously set for that
     * header name. Header names are matched case-insensitively.
     *
     * @param header the header name.
     * @param value  the header value.
     * @return {@code this}.
     */
    public RestClientBuilder setHeader(final String header, final Object value) {
        List<Object> values = new ArrayList<>(1);
        values.add(value);
        this.headers.put(header, values);
        return this;
    }

    /**
     * Applies user-supplied custom HTTP headers, overriding any header of the same name
     * already set on this builder. Call this last so that custom headers take precedence
     * over the headers Jikkou sets itself.
     *
     * @param clientHeaders the custom headers; may be {@code null} or empty.
     * @return {@code this}.
     */
    public RestClientBuilder clientHeaders(final Map<String, String> clientHeaders) {
        if (clientHeaders == null || clientHeaders.isEmpty()) {
            return this;
        }
        clientHeaders.forEach((name, value) -> {
            if (this.headers.containsKey(name)) {
                LOG.warn("Custom client header '{}' overrides the header set by Jikkou.", name);
            }
            setHeader(name, value);
        });
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
        boolean hasTrustStore = sslConfig.trustStoreLocation() != null;
        boolean hasKeyStore = sslConfig.keyStoreLocation() != null;

        if (hasTrustStore || hasKeyStore) {
            TrustManager[] trustManagers;
            try {
                trustManagers = SSLUtils.createTrustManagers(
                    sslConfig.trustStoreLocation(),
                    toCharArrayOrNull(sslConfig.trustStorePassword()),
                    sslConfig.trustStoreType(),
                    KeyManagerFactory.getDefaultAlgorithm());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
                LOG.error("Could not create trust managers for Client Certificate authentication.", e);
                throw new JikkouRuntimeException(e);
            }

            KeyManager[] keyManagers = null;
            if (hasKeyStore) {
                try {
                    keyManagers = SSLUtils.createKeyManagers(
                        sslConfig.keyStoreLocation(),
                        toCharArrayOrNull(sslConfig.keyStorePassword()),
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
            }

            SSLContextFactory sslContextFactory = new SSLContextFactory();
            this.sslContext = sslContextFactory.getSSLContext(keyManagers, trustManagers);
            clientBuilder.sslContext(this.sslContext);
        }

        if (sslConfig.ignoreHostnameVerification()) {
            return sslIgnoreHostnameVerification();
        }
        return this;
    }

    /**
     * Sets the proxy configuration. When the proxy URL is set, requests are routed
     * through it; otherwise standard JVM proxy system properties are honored as a fallback.
     *
     * @param proxyConfig the proxy configuration.
     * @return {@code this}.
     */
    public RestClientBuilder proxyConfig(final ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        return this;
    }

    private static char[] toCharArrayOrNull(String value) {
        return value != null ? value.toCharArray() : null;
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

        // Install a custom Apache engine only when a proxy must be used, so that the
        // default path is completely unchanged for users without any proxy.
        if (shouldUseProxyEngine()) {
            clientBuilder.httpEngine(buildProxyEngine());
        }

        Client client = clientBuilder.build();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(baseUri);
        target.property("org.jboss.resteasy.follow.redirects", followRedirects);

        return target.proxy(resourceInterface);
    }

    private boolean shouldUseProxyEngine() {
        boolean explicit = proxyConfig != null && proxyConfig.hasExplicitProxy();
        return explicit || hasProxySystemProperties();
    }

    private static boolean hasProxySystemProperties() {
        return isNotBlank(System.getProperty("http.proxyHost"))
            || isNotBlank(System.getProperty("https.proxyHost"));
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private ApacheHttpClient43Engine buildProxyEngine() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        if (proxyConfig != null && proxyConfig.hasExplicitProxy()) {
            URI proxyUri = URI.create(proxyConfig.proxyUrl());
            String scheme = proxyUri.getScheme() != null ? proxyUri.getScheme() : "http";
            int port = proxyUri.getPort() != -1
                ? proxyUri.getPort()
                : ("https".equalsIgnoreCase(scheme) ? 443 : 80);
            HttpHost proxyHost = new HttpHost(proxyUri.getHost(), port, scheme);

            List<String> nonProxyHosts = NonProxyHostsRoutePlanner.parse(proxyConfig.nonProxyHosts());
            httpClientBuilder.setRoutePlanner(new NonProxyHostsRoutePlanner(proxyHost, nonProxyHosts));

            if (proxyConfig.hasCredentials()) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    new AuthScope(proxyHost.getHostName(), proxyHost.getPort()),
                    new UsernamePasswordCredentials(proxyConfig.proxyUsername(), proxyConfig.proxyPassword()));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            LOG.info("Routing REST client traffic through proxy: {}://{}:{}",
                scheme, proxyHost.getHostName(), proxyHost.getPort());
        } else {
            // Fallback: honor -Dhttp.proxyHost / -Dhttps.proxyHost / http.proxyUser etc.
            httpClientBuilder.useSystemProperties();
            LOG.info("Routing REST client traffic using JVM proxy system properties");
        }

        if (sslContext != null) {
            httpClientBuilder.setSSLContext(sslContext);
        }
        if (ignoreHostnameVerification) {
            httpClientBuilder.setSSLHostnameVerifier(NO_HOST_NAME_VERIFIER);
        }

        RequestConfig.Builder requestConfig = RequestConfig.custom();
        if (connectTimeout != null) {
            requestConfig.setConnectTimeout((int) connectTimeout.toMillis());
        }
        if (readTimeout != null) {
            requestConfig.setSocketTimeout((int) readTimeout.toMillis());
        }
        httpClientBuilder.setDefaultRequestConfig(requestConfig.build());

        CloseableHttpClient httpClient = httpClientBuilder.build();
        ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);
        engine.setFollowRedirects(followRedirects);
        return engine;
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

    /**
     * A route planner that sends traffic through the given proxy except for hosts
     * matching one of the configured {@code nonProxyHosts} patterns (JVM-style,
     * supporting leading/trailing '*' wildcards).
     */
    static final class NonProxyHostsRoutePlanner extends DefaultProxyRoutePlanner {

        private final List<String> nonProxyHosts;

        NonProxyHostsRoutePlanner(final HttpHost proxy, final List<String> nonProxyHosts) {
            super(proxy);
            this.nonProxyHosts = nonProxyHosts;
        }

        @Override
        protected HttpHost determineProxy(final HttpHost target,
                                          final HttpRequest request,
                                          final HttpContext context) throws HttpException {
            final String host = target.getHostName();
            for (String pattern : nonProxyHosts) {
                if (matches(host, pattern)) {
                    return null; // bypass proxy, connect directly
                }
            }
            return super.determineProxy(target, request, context);
        }

        static boolean matches(final String host, final String rawPattern) {
            final String pattern = rawPattern.trim();
            if (pattern.isEmpty()) {
                return false;
            }
            if (pattern.startsWith("*")) {
                return host.toLowerCase().endsWith(pattern.substring(1).toLowerCase());
            }
            if (pattern.endsWith("*")) {
                return host.toLowerCase().startsWith(pattern.substring(0, pattern.length() - 1).toLowerCase());
            }
            return host.equalsIgnoreCase(pattern);
        }

        static List<String> parse(final String nonProxyHosts) {
            if (nonProxyHosts == null || nonProxyHosts.isBlank()) {
                return List.of();
            }
            return Arrays.stream(nonProxyHosts.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        }
    }
}
