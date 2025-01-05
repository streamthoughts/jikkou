/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata"
})
@Reflectable
public abstract class BaseHasMetadata implements HasMetadata {

    /**
     * Kind attached to the resource.
     */
    @JsonProperty("kind")
    @JsonPropertyDescription("Kind attached to the resource.")
    @NotNull
    protected final String kind;

    /**
     * ApiVersion attached to the resource.
     */
    @JsonProperty("apiVersion")
    @JsonPropertyDescription("ApiVersion attached to the resource.")
    protected final String apiVersion;

    /**
     * Metadata attached to the resource.
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata attached to the resource.")
    protected final ObjectMeta metadata;

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param metadata The metadata object.
     */
    public BaseHasMetadata(final ObjectMeta metadata) {
        this(null, null, metadata);
    }

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     */
    public BaseHasMetadata(@Nullable final String apiVersion,
                           @Nullable final String kind,
                           @Nullable final ObjectMeta metadata) {
        this.apiVersion = Optional.ofNullable(apiVersion).orElseGet(() -> Resource.getApiVersion(this.getClass()));
        this.kind = Optional.ofNullable(kind).orElseGet(() -> Resource.getKind(this.getClass()));
        this.metadata = Optional.ofNullable(metadata).orElse(new ObjectMeta());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getKind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getApiVersion() {
        return apiVersion;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseHasMetadata that = (BaseHasMetadata) o;
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
