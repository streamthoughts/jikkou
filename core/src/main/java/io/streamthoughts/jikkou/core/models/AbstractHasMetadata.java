/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Objects;

public abstract class AbstractHasMetadata implements HasMetadata {

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;

    public AbstractHasMetadata(String kind,
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractHasMetadata that = (AbstractHasMetadata) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(metadata, that.metadata);
    }
    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata);
    }
}
