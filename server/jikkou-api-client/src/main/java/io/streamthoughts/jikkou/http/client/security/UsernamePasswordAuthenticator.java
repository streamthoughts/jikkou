/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
