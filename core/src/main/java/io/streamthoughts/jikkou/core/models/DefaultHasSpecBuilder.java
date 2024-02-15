/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.function.Function;

/**
 * Default class for constructing new resource objects?
 *
 * @param <T> type of the resource.
 * @param <S> Type of the spec.
 */
public final class DefaultHasSpecBuilder<T extends HasMetadata, S> implements HasSpecBuilder<T, S> {

    private ObjectMeta metadata = null;
    private String apiVersion;
    private String kind;
    private S spec;

    private final Function<DefaultHasSpecBuilder<T, S>, T> builder;

    /**
     * Creates a new {@link DefaultHasSpecBuilder} instance.
     *
     * @param builder the builder function.
     */
    public DefaultHasSpecBuilder(Function<DefaultHasSpecBuilder<T, S>, T> builder) {
        this(null, null, builder);
    }

    /**
     * Creates a new {@link DefaultHasSpecBuilder} instance.
     *
     * @param apiVersion the API Version.
     * @param kind       the Kind.
     * @param builder    the builder function.
     */
    public DefaultHasSpecBuilder(String apiVersion,
                                 String kind,
                                 Function<DefaultHasSpecBuilder<T, S>, T> builder) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta metadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withSpec(S spec) {
        this.spec = spec;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public S spec() {
        return spec;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String apiVersion() {
        return apiVersion;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withKind(String kind) {
        this.kind = kind;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String kind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T build() {
        return builder.apply(this);
    }
}
