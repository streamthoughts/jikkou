/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.generics;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceList;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@ApiVersion("core.jikkou.io/v1")
@Kind("GenericResourceList")
@JsonDeserialize
@Reflectable
public final class GenericResourceList<T extends HasMetadata> implements ResourceList<T> {

    /**
     * Kind attached to the resource.
     */
    @JsonProperty("kind")
    @JsonPropertyDescription("Kind attached to the resource.")
    @NotNull
    private final String kind;

    /**
     * ApiVersion attached to the resource.
     */
    @JsonProperty("apiVersion")
    @JsonPropertyDescription("ApiVersion attached to the resource.")
    private final String apiVersion;

    /**
     * Metadata attached to the resource.
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata attached to the resource.")
    private final ObjectMeta metadata;
    private final List<T> items;
    @JsonIgnore
    private final Map<String, Object> additionalProperties;

    /**
     * Creates a new {@link GenericResourceList} instance.
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "items"
    })
    public GenericResourceList(@NotNull final String apiVersion,
                               @NotNull final String kind,
                               @NotNull final ObjectMeta metadata,
                               @NotNull final List<T> items) {
        this(apiVersion, kind, metadata, items, new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link GenericResourceList} instance.
     */
    public GenericResourceList(String apiVersion,
                               String kind,
                               ObjectMeta metadata,
                               List<T> items,
                               Map<String, Object> additionalProperties) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.metadata = metadata;
        this.items = items;
        this.additionalProperties = additionalProperties;
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("kind")
    @Override
    public String getKind() {
        return Optional.ofNullable(kind).orElse(Resource.getKind(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("apiVersion")
    @Override
    public String getApiVersion() {
        return Optional.ofNullable(apiVersion).orElse(Resource.getApiVersion(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("metadata")
    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata withMetadata(ObjectMeta metadata) {
        return new GenericResourceList<>(
            apiVersion,
            kind,
            metadata,
            items,
            additionalProperties
        );
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericResourceList<?> that = (GenericResourceList<?>) o;
        return Objects.equals(kind, that.kind) &&
            Objects.equals(apiVersion, that.apiVersion) &&
            Objects.equals(metadata, that.metadata) &&
            Objects.equals(items, that.items) &&
            Objects.equals(additionalProperties, that.additionalProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata, items, additionalProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getItems() {
        return items;
    }

    public Builder<T> toBuilder() {
        return new Builder<T>()
            .withKind(kind)
            .withApiVersion(apiVersion)
            .withItems(items);
    }

    /**
     * Base builder for constructing new specific resource objects.
     *
     * @param <T> type of the items.
     */
    public static class Builder<T extends HasMetadata> {
        protected String kind;
        protected String apiVersion;
        protected ObjectMeta metadata;
        protected List<T> items;

        /**
         * Sets the resource Kind.
         *
         * @param kind The Kind.
         * @return {@code this}
         */
        public Builder<T> withKind(final String kind) {
            this.kind = kind;
            return this;
        }

        /**
         * Sets the resource ApiVersion.
         *
         * @param apiVersion The ApiVersion
         * @return {@code this}
         */
        public Builder<T> withApiVersion(final String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        /**
         * Sets the spec attached to the resource.
         *
         * @param items The items.
         * @return {@code this}
         */
        public Builder<T> withItems(List<T> items) {
            this.items = items;
            return this;
        }

        /**
         * Sets the metadata attached to the resource.
         *
         * @param metadata The {@link ObjectMeta}.
         * @return {@code this}
         */
        public Builder<T> withMetadata(final ObjectMeta metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Builds the specific resource object.
         *
         * @return a new {@link Object}.
         */
        public GenericResourceList<T> build() {
            return new GenericResourceList<>(
                apiVersion,
                kind,
                metadata,
                items
            );
        }
    }
}
