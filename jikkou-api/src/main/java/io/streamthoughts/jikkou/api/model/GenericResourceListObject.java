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
package io.streamthoughts.jikkou.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.CoreAnnotations;
import io.streamthoughts.jikkou.annotation.ApiVersion;
import io.streamthoughts.jikkou.annotation.Description;
import io.streamthoughts.jikkou.annotation.Kind;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@With
@Description("")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "items"
})
@ApiVersion("core.jikkou.io/v1beta2")
@Kind("GenericResourceListObject")
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
public final class GenericResourceListObject<T extends HasMetadata> implements ResourceListObject<T>  {

    public static GenericResourceListObject<HasMetadata> empty() {
        return new GenericResourceListObject<>(Collections.emptyList());
    }

    public static GenericResourceListObject<HasMetadata> of(final HasItems items) {
        return new GenericResourceListObject<>(new ArrayList<>(items.getItems()));
    }

    public static GenericResourceListObject<HasMetadata> of(final HasMetadata... resources) {
        return new GenericResourceListObject<>(Arrays.asList(resources));
    }

    public static GenericResourceListObject<HasMetadata> of(final List<HasMetadata> resources) {
        return new GenericResourceListObject<>(resources);
    }

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;
    private final List<T> items;

    /**
     * Creates a new {@link GenericResourceListObject} instance.
     */

    public GenericResourceListObject(final List<? extends T> items) {
        this(null, null, new ObjectMeta(), items);
    }


    /**
     * Creates a new {@link GenericResourceListObject} instance.
     */
    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "items"
    })
    public GenericResourceListObject(@Nullable final String kind,
                                     @Nullable final String apiVersion,
                                     @Nullable final ObjectMeta metadata,
                                     @NotNull final List<? extends T> items) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.items = new ArrayList<>(items);
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("kind")
    @Override
    public String getKind() {
        return Optional.ofNullable(kind).orElse(HasMetadata.getKind(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("apiVersion")
    @Override
    public String getApiVersion() {
        return Optional.ofNullable(apiVersion).orElse(HasMetadata.getApiVersion(this.getClass()));
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

    /** {@inheritDoc} */
    @Override
    public List<T> getItems() {
        return items;
    }
}
