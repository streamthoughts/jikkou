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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * ApiResourceChangeList.
 *
 * @param kind       The resource Kind.
 * @param apiVersion The API version.
 * @param metadata   The resource metadata.
 * @param items      The changes.
 */
@ApiVersion("core.jikkou.io/v1")
@Kind("ApiResourceChangeList")
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "metadata",
        "items"
})
@Reflectable
@JsonDeserialize
public record ApiResourceChangeList(@JsonProperty("kind") @NotNull String kind,
                                    @JsonProperty("apiVersion") @NotNull String apiVersion,
                                    @JsonProperty("metadata") @NotNull ObjectMeta metadata,
                                    @JsonProperty("items") @NotNull List<ResourceChange> items) implements ResourceListObject<ResourceChange> {

    /**
     * Creates a new {@link ApiResourceChangeList} instance.
     */
    @ConstructorProperties({
            "kind",
            "apiVersion",
            "items"
    })
    public ApiResourceChangeList {
    }

    public ApiResourceChangeList(@NotNull List<ResourceChange> items) {
        this(
                Resource.getKind(ApiResourceChangeList.class),
                Resource.getApiVersion(ApiResourceChangeList.class),
                null,
                items
        );
    }

    @Override
    public ApiResourceChangeList withMetadata(ObjectMeta metadata) {
        return new ApiResourceChangeList(
                kind,
                apiVersion,
                metadata,
                items
        );
    }

    @JsonIgnore
    @Override
    public List<ResourceChange> getItems() {
        return items;
    }

    @JsonIgnore
    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }
}
