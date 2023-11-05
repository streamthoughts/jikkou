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
package io.streamthoughts.jikkou.core.models.generics;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
public final class GenericResourceListObject<T extends HasMetadata> implements ResourceListObject<T> {

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;
    private final List<T> items;
    @JsonIgnore
    private final Map<String, Object> additionalProperties;

    /**
     * Creates a new {@link GenericResourceListObject} instance.
     */
    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "items"
    })
    public GenericResourceListObject(@NotNull final String kind,
                                     @NotNull final String apiVersion,
                                     @NotNull final ObjectMeta metadata,
                                     @NotNull final List<T> items) {
        this(kind, apiVersion, metadata, items, new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link GenericResourceListObject} instance.
     */
    public GenericResourceListObject(String kind,
                                     String apiVersion,
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
    public HasMetadata withMetadata(ObjectMeta objectMeta) {
        return new GenericResourceListObject<>(
                kind,
                apiVersion,
                objectMeta,
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

    /** {@inheritDoc} */
    @Override
    public List<T> getItems() {
        return items;
    }
}
