/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.security;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Token-based authentication.
 */
public final class BearerTokenAuthenticator implements Authenticator {

    private final Supplier<BearerToken> tokenSupplier;

    /**
     * Creates a new {@link BearerTokenAuthenticator} instance.
     *
     * @param tokenSupplier the object supplying the {@link BearerToken}.
     */
    public BearerTokenAuthenticator(final Supplier<BearerToken> tokenSupplier) {
        this.tokenSupplier = Objects.requireNonNull(
                tokenSupplier,
                "tokenSupplier cannot be null"
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Request authenticate(final @Nullable Route route, final @NotNull Response response) {
        return Optional.ofNullable(tokenSupplier.get())
                .map(BearerToken::value)
                .map(token -> response.request().newBuilder().header("Authorization", "Bearer " + token))
                .orElseGet(() -> response.request().newBuilder())
                .build();
    }
}
