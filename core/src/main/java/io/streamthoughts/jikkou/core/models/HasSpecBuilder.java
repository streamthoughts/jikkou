/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

/**
 * Interface for constructing new resources.
 *
 * @param <T> type of the resource.
 * @param <S> type of the specification.
 */
public interface HasSpecBuilder<T extends HasMetadata, S> {

    /**
     * Gets the resource API Version.
     *
     * @return the API Version.
     */
    String apiVersion();

    /**
     * Sets the resource ApiVersion.
     *
     * @param apiVersion the API Version.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withApiVersion(String apiVersion);

    /**
     * Sets the resource Kind.
     *
     * @param kind the Kind.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withKind(String kind);

    /**
     * Gets the resource Kind.
     *
     * @return the Kind.
     */
    String kind();

    /**
     * Sets the resource metadata.
     *
     * @param metadata The metadata.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withMetadata(ObjectMeta metadata);

    /**
     * Gets the resource metadata.
     *
     * @return The {@link ObjectMeta}.
     */
    ObjectMeta metadata();

    /**
     * Gets the resource specification.
     *
     * @return The spec.
     */
    S spec();

    /**
     * Sets the resource specification.
     *
     * @param spec The spec.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withSpec(S spec);

    /**
     * Creates a new resource.
     *
     * @return The new resource.
     */
    T build();
}
