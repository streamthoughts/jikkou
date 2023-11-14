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

import static io.streamthoughts.jikkou.http.client.Config.DEFAULT_FALLBACK_HOST;
import static io.streamthoughts.jikkou.http.client.Config.DEFAULT_FALLBACK_PORT;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import io.streamthoughts.jikkou.http.client.interceptors.ClientLoggingInterceptor;
import io.streamthoughts.jikkou.http.client.security.SSLContextFactory;
import io.streamthoughts.jikkou.http.client.security.SSLUtils;
import io.streamthoughts.jikkou.http.client.serdes.JSON;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.jetbrains.annotations.NotNull;

public final class ApiClientBuilder {

    public static final AllowAllHostNameVerifier NO_HOST_NAME_VERIFIER = new AllowAllHostNameVerifier();
    public static final String DEFAULT_USER_AGENT = "Jikkou HTTP Client";
    private OkHttpClient httpClient;
    private String basePath = "http://" + DEFAULT_FALLBACK_HOST + ":" + DEFAULT_FALLBACK_PORT;
    private boolean debugging = false;
    private boolean verifyingSsl = true;
    private KeyManager[] keyManagers = {};
    private TrustManager[] trustManagers = {};
    private SSLContext sslContext;
    private ClientLoggingInterceptor loggingInterceptor;
    private final Map<String, String> defaultHeaderMap;
    private final Map<String, String> defaultCookieMap;

    /**
     * Helper method to create a new {@link ApiClientBuilder} instance.
     *
     * @return a new {@link ApiClientBuilder} instance.
     */
    public static ApiClientBuilder builder() {
        return new ApiClientBuilder();
    }

    /**
     * Basic constructor for ApiClientBuilder.
     */
    private ApiClientBuilder() {
        this(new OkHttpClient.Builder().build());
    }

    /**
     * Basic constructor for ApiClientBuilder.
     */
    public ApiClientBuilder(@NotNull OkHttpClient client) {
        this(client, new HashMap<>(), new HashMap<>());
    }

    /**
     * Basic constructor for ApiClientBuilder.
     */
    ApiClientBuilder(@NotNull OkHttpClient client,
                     @NotNull Map<String, String> defaultHeaderMap,
                     @NotNull Map<String, String> defaultCookieMap) {

        httpClient = Objects.requireNonNull(client, "client must not be null");
        this.defaultCookieMap = new HashMap<>(defaultCookieMap);
        this.defaultHeaderMap = new HashMap<>(defaultHeaderMap);
        init();
    }


    private void init() {
        verifyingSsl = true;
        withUserAgent(DEFAULT_USER_AGENT);
    }

    /**
     * Configure whether to verify certificate and hostname when making https requests. Default to
     * true. NOTE: Do NOT set to false in production name, otherwise you would face multiple types of
     * cryptographic attacks.
     *
     * @param verifyingSsl True to verify TLS/SSL connection
     * @return ApiClient
     */
    public ApiClientBuilder withVerifyingSsl(boolean verifyingSsl) {
        this.verifyingSsl = verifyingSsl;
        applySslSettings();
        return this;
    }

    /**
     * Set the User-Agent header's value (by adding to the default header map).
     *
     * @param userAgent HTTP request's user agent
     * @return ApiClient
     */
    public ApiClientBuilder withUserAgent(String userAgent) {
        withDefaultHeader("User-Agent", userAgent);
        return this;
    }

    /**
     * Add a default header.
     *
     * @param key   The header's key
     * @param value The header's value
     * @return ApiClient
     */
    public ApiClientBuilder withDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
        return this;
    }

    /**
     * Add a default cookie.
     *
     * @param key   The cookie's key
     * @param value The cookie's value
     * @return ApiClient
     */
    public ApiClientBuilder withDefaultCookie(String key, String value) {
        defaultCookieMap.put(key, value);
        return this;
    }

    /**
     * Set base path
     *
     * @param basePath Base path of the URL (e.g. <a href="http://localhost">...</a>)
     * @return An instance of OkHttpClient
     */
    public ApiClientBuilder withBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    /**
     * Sets the authenticator used to respond to challenges from origin servers.
     */
    public ApiClientBuilder withAuthenticator(final Authenticator authenticator) {
        httpClient = httpClient.newBuilder().authenticator(authenticator).build();
        return this;
    }

    /**
     * Enable/disable debugging for this API client.
     *
     * @param debugging To enable (true) or disable (false) debugging
     * @return ApiClient
     */
    public ApiClientBuilder withDebugging(boolean debugging) {
        if (debugging != this.debugging) {
            if (debugging) {
                loggingInterceptor = new ClientLoggingInterceptor();
                httpClient = httpClient.newBuilder().addInterceptor(loggingInterceptor).build();
            } else {
                final OkHttpClient.Builder builder = httpClient.newBuilder();
                builder.interceptors().remove(loggingInterceptor);
                httpClient = builder.build();
                loggingInterceptor = null;
            }
        }
        this.debugging = debugging;
        return this;
    }

    /**
     * Sets the connect timeout (in milliseconds). A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE}.
     *
     * @param connectionTimeout connection timeout in milliseconds
     * @return Api client
     */
    public ApiClientBuilder withConnectTimeout(int connectionTimeout) {
        httpClient = httpClient.newBuilder().connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS).build();
        return this;
    }

    /**
     * Sets the read timeout (in milliseconds). A value of 0 means no timeout, otherwise values must
     * be between 1 and {@link Integer#MAX_VALUE}.
     *
     * @param readTimeout read timeout in milliseconds
     * @return Api client
     */
    public ApiClientBuilder withReadTimeout(int readTimeout) {
        httpClient = httpClient.newBuilder().readTimeout(readTimeout, TimeUnit.MILLISECONDS).build();
        return this;
    }

    /**
     * Sets the write timeout (in milliseconds). A value of 0 means no timeout, otherwise values must
     * be between 1 and {@link Integer#MAX_VALUE}.
     *
     * @param writeTimeout connection timeout in milliseconds
     * @return Api client
     */
    public ApiClientBuilder withWriteTimeout(int writeTimeout) {
        httpClient = httpClient.newBuilder().writeTimeout(writeTimeout, TimeUnit.MILLISECONDS).build();
        return this;
    }

    /**
     * Configure client keys to use for authorization in an SSL session. Use null to reset to default.
     *
     * @param keyManagers The KeyManagers to use
     * @return ApiClient
     */
    public ApiClientBuilder withSslKeyManagers(final KeyManager[] keyManagers) {
        this.keyManagers = Arrays.copyOf(keyManagers, keyManagers.length);
        applySslSettings();
        return this;
    }

    public ApiClientBuilder withSslContext(final SSLContext sslContext) {
        this.sslContext = sslContext;
        applySslSettings();
        return this;
    }

    /**
     * Configure client keys to use for authorization in an SSL session. Use null to reset to default.
     *
     * @param trustManagers The TrustManager to use
     * @return ApiClient
     */
    public ApiClientBuilder withSslTrustManagers(final TrustManager[] trustManagers) {
        this.trustManagers = Arrays.copyOf(trustManagers, trustManagers.length);
        applySslSettings();
        return this;
    }

    private void applySslSettings() {
        try {
            HostnameVerifier hostnameVerifier = verifyingSsl ? OkHostnameVerifier.INSTANCE : NO_HOST_NAME_VERIFIER;
            SSLContext sslContext = this.sslContext;
            TrustManager[] trustManagers = {};
            if (sslContext == null) {
                if (!verifyingSsl) {
                    trustManagers =
                            new TrustManager[]{
                                    new X509TrustManager() {
                                        @Override
                                        public void checkClientTrusted(
                                                X509Certificate[] chain, String authType) {
                                        }

                                        @Override
                                        public void checkServerTrusted(
                                                X509Certificate[] chain, String authType) {
                                        }

                                        @Override
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return new X509Certificate[]{};
                                        }
                                    }
                            };
                } else {
                    trustManagers = this.trustManagers;
                }
                sslContext = new SSLContextFactory().getSSLContext(keyManagers, trustManagers);
            }

            if (trustManagers == null) {
                SSLUtils.createTrustManagers(null, TrustManagerFactory.getDefaultAlgorithm());
            }

            httpClient =
                    httpClient
                            .newBuilder()
                            .sslSocketFactory(
                                    sslContext.getSocketFactory(),
                                    SSLUtils.getX509TrustManager(trustManagers))
                            .hostnameVerifier(hostnameVerifier)
                            .build();
        } catch (GeneralSecurityException e) {
            throw new JikkouApiClientException(e);
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

    public ApiClient build() {
        return new ApiClient(httpClient)
                .setBasePath(basePath)
                .setDefaultCookies(defaultCookieMap)
                .setDefaultHeaders(defaultHeaderMap)
                .setJSON(new JSON.Jackson(Jackson.JSON_OBJECT_MAPPER));
    }
}
