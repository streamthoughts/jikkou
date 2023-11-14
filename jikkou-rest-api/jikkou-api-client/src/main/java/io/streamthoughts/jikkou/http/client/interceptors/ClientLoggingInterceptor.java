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
package io.streamthoughts.jikkou.http.client.interceptors;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that delegates to {@link HttpLoggingInterceptor}
 */
public final class ClientLoggingInterceptor implements Interceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoggingInterceptor.class);

    private final HttpLoggingInterceptor delegate;

    /**
     * Creates a new {@link ClientLoggingInterceptor} instance.
     */
    public ClientLoggingInterceptor() {
        this(HttpLoggingInterceptor.Level.BODY);
    }
    /**
     * Creates a new {@link ClientLoggingInterceptor} instance.
     */
    public ClientLoggingInterceptor(HttpLoggingInterceptor.Level level) {
        delegate = new HttpLoggingInterceptor(LOG::info).setLevel(level);
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        return delegate.intercept(chain);
    }
}
