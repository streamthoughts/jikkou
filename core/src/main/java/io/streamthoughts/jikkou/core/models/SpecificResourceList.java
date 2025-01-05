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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Base class for defining list of specific resource.
 *
 * @param <T> the resource type.
 * @param <E> the resource items.
 * @see HasMetadata
 * @see HasSpec
 * @see Resource
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@JsonDeserialize
@Reflectable
public abstract class SpecificResourceList<T extends SpecificResourceList<T, E>, E extends HasMetadata> extends BaseHasMetadata implements ResourceList<E> {

    /**
     * List of specification objects
     */
    @JsonProperty("items")
    @JsonPropertyDescription("List of specification objects.")
    @NotNull
    protected final List<E> items;

    /**
     * Creates a new {@link SpecificResourceList} instance.
     *
     * @param metadata The metadata object.
     * @param items    The resource items.
     */
    public SpecificResourceList(final ObjectMeta metadata,
                                final List<E> items) {
        this(
            null,
            null,
            metadata,
            items
        );
    }

    /**
     * Creates a new {@link SpecificResourceList} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     * @param items      The resource items.
     */
    public SpecificResourceList(final String apiVersion,
                                final String kind,
                                final ObjectMeta metadata,
                                final List<E> items) {
        super(apiVersion, kind, metadata);
        this.items = items;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta getMetadata() {
        return Optional.ofNullable(metadata).orElse(new ObjectMeta()).toBuilder()
            .withAnnotation(CoreAnnotations.JIKKOU_IO_ITEMS_COUNT, items.size())
            .build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<E> getItems() {
        return items;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T withMetadata(final ObjectMeta metadata) {
        return toBuilder().withMetadata(metadata).build();
    }

    public abstract Builder<?, T, E> toBuilder();

    /**
     * Base builder for constructing new specific resource objects.
     *
     * @param <B> type of the builder.
     * @param <I> type of the items.
     */
    public static abstract class Builder<B extends Builder<B, T, I>, T extends HasMetadata & HasItems, I extends HasMetadata> {
        protected String kind;
        protected String apiVersion;
        protected ObjectMeta metadata;
        protected ObjectTemplate template;
        protected List<I> items;

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
         * @param items The items.
         * @return {@code this}
         */
        @SuppressWarnings("unchecked")
        public B withItems(List<I> items) {
            this.items = items;
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
