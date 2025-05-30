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

/**
 * Base class for defining a specific resource.
 *
 * @param <T> the resource type.
 * @param <S> the resource spec object type.
 * @see HasMetadata
 * @see HasSpec
 * @see Resource
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec"
})
@Reflectable
public abstract class SpecificResource<T extends SpecificResource<T, S>, S> extends BaseHasMetadata implements HasMetadata, HasSpec<S> {

    /**
     * Specification object attached to the resource.
     */
    @JsonProperty("spec")
    @JsonPropertyDescription("Specification object attached to the resource.")
    @NotNull
    protected final S spec;

    /**
     * Creates a new {@link SpecificResource} instance.
     *
     * @param metadata The metadata object.
     * @param spec     The spec object.
     */
    public SpecificResource(final ObjectMeta metadata,
                            final S spec) {
        this(
            null,
            null,
            metadata,
            spec
        );
    }

    /**
     * Creates a new {@link SpecificResource} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     * @param spec       The resource spec.
     */
    public SpecificResource(final String apiVersion,
                            final String kind,
                            final ObjectMeta metadata,
                            final S spec) {
        super(apiVersion, kind, metadata);
        this.spec = spec;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public S getSpec() {
        return spec;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T withMetadata(final ObjectMeta metadata) {
        return toBuilder().withMetadata(metadata).build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecificResource<?, ?> that = (SpecificResource<?, ?>) o;
        return Objects.equals(kind, that.kind) &&
            Objects.equals(apiVersion, that.apiVersion) &&
            Objects.equals(metadata, that.metadata) &&
            Objects.equals(spec, that.spec);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata, spec);
    }

    /**
     * Creates a new builder from this object.
     *
     * @return a new {@link Builder}.
     */
    public abstract Builder<?, T, S> toBuilder();

    /**
     * Base builder for constructing new specific resource objects.
     *
     * @param <B> type of the builder.
     * @param <S> type of the specification.
     */
    public static abstract class Builder<B extends Builder<B, T, S>, T extends HasMetadata & HasSpec<S>, S> {
        protected String kind;
        protected String apiVersion;
        protected ObjectMeta metadata;
        protected ObjectTemplate template;
        protected S spec;

        /**
         * Sets the resource Kind.
         *
         * @param kind The Kind.
         * @return {@code this}
         */
        @SuppressWarnings("unchecked")
        public B withKind(final String kind) {
            this.kind = kind;
            return (B) this;
        }

        /**
         * Sets the resource ApiVersion.
         *
         * @param apiVersion The ApiVersion
         * @return {@code this}
         */
        @SuppressWarnings("unchecked")
        public B withApiVersion(final String apiVersion) {
            this.apiVersion = apiVersion;
            return (B) this;
        }

        /**
         * Sets the spec attached to the resource.
         *
         * @param spec The spec.
         * @return {@code this}
         */
        @SuppressWarnings("unchecked")
        public B withSpec(S spec) {
            this.spec = spec;
            return (B) this;
        }

        /**
         * Sets the metadata attached to the resource.
         *
         * @param metadata The {@link ObjectMeta}.
         * @return {@code this}
         */
        @SuppressWarnings("unchecked")
        public B withMetadata(final ObjectMeta metadata) {
            this.metadata = metadata;
            return (B) this;
        }

        /**
         * Builds the specific resource object.
         *
         * @return a new {@link Object}.
         */
        public abstract T build();
    }
}
