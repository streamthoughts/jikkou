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
package io.streamthoughts.jikkou.http.client.security;

import java.util.Objects;
import java.util.function.Supplier;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * UsernamePasswordAuthenticator.
 */
public final class UsernamePasswordAuthenticator implements Authenticator {

    private final Supplier<UsernamePasswordCredential> credentialSupplier;

    /**
     * Creates a new {@link UsernamePasswordAuthenticator} instance.
     *
     * @param credentialSupplier the object supplying the {@link UsernamePasswordCredential}.
     */
    public UsernamePasswordAuthenticator(final Supplier<UsernamePasswordCredential> credentialSupplier) {
        this.credentialSupplier = Objects.requireNonNull(
                credentialSupplier,
                "credentialSupplier cannot be null"
        );
    }

    /** {@inheritDoc} **/
    @Override
    public Request authenticate(final @Nullable Route route, final @NotNull Response response) {
        final UsernamePasswordCredential credential = credentialSupplier.get();
        final String password = credential.password();
        final String username = credential.username();

        if (username != null && password != null) {
            final String basic = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", basic).build();
        }
        return response.request().newBuilder().build();
    }
}
