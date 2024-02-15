/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Top-level exception for the RestClient.
 */
public class RestClientException extends JikkouRuntimeException {

    private final Throwable cause;
    private final String requestMethod;
    private final String requestUrl;

    /**
     * Creates a new {@link RestClientException} instance.
     *
     * @param cause         The cause.
     */
    public RestClientException(@NotNull Throwable cause) {
        this(cause, null, null);
    }

    /**
     * Creates a new {@link RestClientException} instance.
     *
     * @param cause         The cause.
     * @param requestUrl    The request URL.
     * @param requestMethod the request Method.
     */
    public RestClientException(@NotNull Throwable cause,
                               @Nullable String requestUrl,
                               @Nullable String requestMethod) {
        super(cause);
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.cause = cause;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getLocalizedMessage() {
        return cause.getLocalizedMessage();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getMessage() {
        return cause.getMessage();
    }

    /**
     * Get the executed url.
     *
     * @return the string request url.
     */
    public Optional<String> requestUrl() {
        return Optional.ofNullable(requestUrl);
    }

    /**
     * Get the http method.
     *
     * @return the string method
     */
    public Optional<String> requestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    /**
     * Get the HTTP response.
     *
     * @return the HTTP response.
     */
    public Optional<Response> response() {
        if (cause instanceof WebApplicationException webApplicationException) {
            return Optional.ofNullable(webApplicationException.getResponse());
        }
        return Optional.empty();
    }

    /**
     * Get the response entity.
     *
     * @return the string response entity, or {@code null}.
     */
    public String getResponseEntity() {
        return getResponseEntity(String.class);
    }

    /**
     * Get the response entity.
     *
     * @param entityType the entity type.
     * @return the response entity, or {@code null}.
     */
    public <T> T getResponseEntity(Class<T> entityType) {
        Optional<Response> optional = response();
        if (optional.isPresent()) {
            Response response = optional.get();
            if (response.hasEntity()) {
                return response.readEntity(entityType);
            }
        }
        return null;
    }
}
