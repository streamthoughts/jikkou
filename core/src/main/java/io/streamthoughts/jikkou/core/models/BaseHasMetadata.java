/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Optional;

public class BaseHasMetadata implements HasMetadata {

    private String kind;
    private String apiVersion;
    private ObjectMeta metadata;

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     */
    public BaseHasMetadata() {
        this(new ObjectMeta());
    }

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param metadata The object metadata.
     */
    public BaseHasMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param kind       The resource kind.
     * @param apiVersion The resource API Version.
     * @param metadata   The object metadata..
     */
    public BaseHasMetadata(String kind,
                           String apiVersion,
                           ObjectMeta metadata) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getKind() {
        return Optional.of(kind).orElse(Resource.getKind(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getApiVersion() {
        return Optional.of(apiVersion).orElse(Resource.getApiVersion(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata withMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }
}
