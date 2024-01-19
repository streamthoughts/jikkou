/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("")
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@ApiVersion("core.jikkou.io/v1beta2")
@Kind("GenericResourceList")
@JsonDeserialize
@Reflectable
public class DefaultResourceListObject<T extends HasMetadata> implements ResourceListObject<T> {

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;
    private final List<T> items;

    /**
     * Creates a new {@link DefaultResourceListObject} instance.
     */

    public DefaultResourceListObject(final List<? extends T> items) {
        this(null, null, new ObjectMeta(), items);
    }

    /**
     * Creates a new {@link DefaultResourceListObject} instance.
     */
    @ConstructorProperties({
        "kind",
        "apiVersion",
        "metadata",
        "items"
    })
    public DefaultResourceListObject(@Nullable final String kind,
                                     @Nullable final String apiVersion,
                                     @Nullable final ObjectMeta metadata,
                                     @NotNull final List<? extends T> items) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.items = new ArrayList<>(items);
        this.metadata = metadata;
    }

    public static <T extends HasMetadata> DefaultResourceListObjectBuilder<T> builder() {
        return new DefaultResourceListObjectBuilder<T>();
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
        return Optional.ofNullable(metadata).orElse(new ObjectMeta()).toBuilder()
            .withAnnotation(CoreAnnotations.JIKKOU_IO_ITEMS_COUNT, items.size())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @JsonProperty("items")
    @Override
    public List<T> getItems() {
        return items;
    }

    public DefaultResourceListObject<T> withKind(String kind) {
        return new DefaultResourceListObject<T>(kind, this.apiVersion, this.metadata, this.items);
    }

    public DefaultResourceListObject<T> withApiVersion(String apiVersion) {
        return new DefaultResourceListObject<T>(this.kind, apiVersion, this.metadata, this.items);
    }

    @Override
    public DefaultResourceListObject<T> withMetadata(ObjectMeta metadata) {
        return new DefaultResourceListObject<T>(this.kind, this.apiVersion, metadata, this.items);
    }

    public DefaultResourceListObject<T> withItems(List<T> items) {
        return new DefaultResourceListObject<T>(this.kind, this.apiVersion, this.metadata, items);
    }

    public DefaultResourceListObjectBuilder<T> toBuilder() {
        return new DefaultResourceListObjectBuilder<T>()
            .withKind(this.kind)
            .withApiVersion(this.apiVersion)
            .withMetadata(this.metadata)
            .withItems(this.items);
    }

    public static class DefaultResourceListObjectBuilder<T extends HasMetadata> {
        private String kind;
        private String apiVersion;
        private ObjectMeta metadata;
        private List<T> items;

        DefaultResourceListObjectBuilder() {
        }

        public DefaultResourceListObjectBuilder<T> withKind(String kind) {
            this.kind = kind;
            return this;
        }

        public DefaultResourceListObjectBuilder<T> withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public DefaultResourceListObjectBuilder<T> withMetadata(ObjectMeta metadata) {
            this.metadata = metadata;
            return this;
        }

        public DefaultResourceListObjectBuilder<T> withItems(List<T> items) {
            this.items = items;
            return this;
        }

        public DefaultResourceListObject<T> build() {
            return new DefaultResourceListObject<T>(this.kind, this.apiVersion, this.metadata, this.items);
        }
    }
}
