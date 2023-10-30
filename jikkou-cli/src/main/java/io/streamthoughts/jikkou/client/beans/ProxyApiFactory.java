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
package io.streamthoughts.jikkou.client.beans;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.http.client.ApiClient;
import io.streamthoughts.jikkou.http.client.ApiClientBuilder;
import io.streamthoughts.jikkou.http.client.DefaultJikkouApiClient;
import io.streamthoughts.jikkou.http.client.JikkouApiProxy;
import io.streamthoughts.jikkou.http.client.security.UsernamePasswordAuthenticator;
import io.streamthoughts.jikkou.http.client.security.UsernamePasswordCredential;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Factory
public final class ProxyApiFactory {

    @Inject
    ProxyConfiguration configuration;

    @Singleton
    @Requires(bean = ProxyConfiguration.class)
    @SuppressWarnings("rawtypes")
    public JikkouApi.ApiBuilder proxyApiBuilder(JikkouContext context) {
        ApiClientBuilder builder = ApiClientBuilder.builder();
        builder
                .withBasePath(configuration.url())
                .withDebugging(configuration.debugging())
                .withWriteTimeout(getWriteTimeout())
                .withReadTimeout(getReadTimeout())
                .withConnectTimeout(getConnectionTimeout());

        Optional.ofNullable(configuration.basicAuth())
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

        ApiClient client = builder.build();
        return new JikkouApiProxy.Builder(
                context.getExtensionFactory(),
                new DefaultJikkouApiClient(client)
        );
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
