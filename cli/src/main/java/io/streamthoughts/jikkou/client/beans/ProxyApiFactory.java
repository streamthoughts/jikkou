/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.beans;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.http.client.ApiClientBuilder;
import io.streamthoughts.jikkou.http.client.DefaultJikkouApiClient;
import io.streamthoughts.jikkou.http.client.JikkouApiClient;
import io.streamthoughts.jikkou.http.client.JikkouApiProxy;
import io.streamthoughts.jikkou.http.client.security.BearerToken;
import io.streamthoughts.jikkou.http.client.security.BearerTokenAuthenticator;
import io.streamthoughts.jikkou.http.client.security.UsernamePasswordAuthenticator;
import io.streamthoughts.jikkou.http.client.security.UsernamePasswordCredential;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Requires(bean = ProxyConfiguration.class)
@Factory
public final class ProxyApiFactory {

    @Inject
    ProxyConfiguration configuration;

    @Singleton
    public JikkouApiClient apiClient() {
        ApiClientBuilder builder = ApiClientBuilder.builder();
        builder
            .withBasePath(configuration.url())
            .withDebugging(configuration.debugging())
            .withWriteTimeout(getWriteTimeout())
            .withReadTimeout(getReadTimeout())
            .withConnectTimeout(getConnectionTimeout());

        ProxyConfiguration.Security security = configuration.security();

        // Bearer Token based authentication.
        security.accessToken()
            .ifPresent(accessToken ->
                builder.withAuthenticator(
                    new BearerTokenAuthenticator(() -> new BearerToken(accessToken))
                )
            );
        // UsernamePassword based authentication.
        Optional.ofNullable(security.basicAuth())
            .ifPresent(basicAuth -> {
                Optional<String> username = basicAuth.username();
                Optional<String> password = basicAuth.password();
                if (username.isPresent() && password.isPresent()) {
                    builder.withAuthenticator(new UsernamePasswordAuthenticator(() -> {
                        return new UsernamePasswordCredential(
                            username.get(),
                            password.get()
                        );
                    }));
                }
            });

        return new DefaultJikkouApiClient(builder.build());
    }

    @Singleton
    @SuppressWarnings("rawtypes")
    public JikkouApi.ApiBuilder proxyApiBuilder(JikkouContext context, JikkouApiClient apiClient) {
        return new JikkouApiProxy.Builder(context.getExtensionFactory(), context.getResourceRegistry(), apiClient);
    }

    @NotNull
    private Integer getConnectionTimeout() {
        return configuration.connectTimeout()
            .orElse(configuration.defaultTimeout());
    }

    @NotNull
    private Integer getReadTimeout() {
        return configuration.readTimeout()
            .orElse(configuration.defaultTimeout());
    }

    @NotNull
    private Integer getWriteTimeout() {
        return configuration.writeTimeout()
            .orElse(configuration.defaultTimeout());
    }
}
