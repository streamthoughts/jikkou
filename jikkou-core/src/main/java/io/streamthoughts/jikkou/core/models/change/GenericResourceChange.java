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
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.DefaultHasSpecBuilder;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpecBuilder;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.Resource;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "spec"
})
@ApiVersion("core.jikkou.io/v1beta2")
@Kind("GenericResourceChange")
@JsonDeserialize
@Reflectable
public final class GenericResourceChange implements ResourceChange {

    /**
     * Creates a builder for creating a new resource change.
     *
     * @return a new {@link HasSpecBuilder}.
     */
    public static HasSpecBuilder<GenericResourceChange, GenericResourceChangeSpec> builder() {
        return new DefaultHasSpecBuilder<>(build -> new GenericResourceChange(
                build.kind(),
                build.apiVersion(),
                build.metadata(),
                build.spec()
        ));
    }

    /**
     * Creates a builder for creating a new resource change.
     *
     * @return a new {@link HasSpecBuilder}.
     */
    public static HasSpecBuilder<GenericResourceChange, GenericResourceChangeSpec> builder(Class<? extends Resource> resource) {
        return new DefaultHasSpecBuilder<>(
                Resource.getApiVersion(resource),
                ResourceChange.getChangeKindFromResource(resource),
                build -> new GenericResourceChange(
                        build.kind(),
                        build.apiVersion(),
                        build.metadata(),
                        build.spec()
                ));
    }

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;
    private final GenericResourceChangeSpec spec;

    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "spec"
    })
    public GenericResourceChange(@NotNull String kind,
                                 @NotNull String apiVersion,
                                 @NotNull ObjectMeta metadata,
                                 @NotNull GenericResourceChangeSpec spec) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.metadata = metadata;
        this.spec = spec;
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
        return Optional.ofNullable(metadata).orElse(new ObjectMeta());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata withMetadata(ObjectMeta metadata) {
        return new GenericResourceChange(apiVersion, kind, metadata, spec);
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("spec")
    @Override
    public GenericResourceChangeSpec getSpec() {
        return spec;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "GenericResourceChange[" +
                "kind=" + kind +
                ", apiVersion=" + apiVersion +
                ", metadata=" + metadata +
                ", spec=" + spec +
                ']';
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GenericResourceChange) obj;
        return Objects.equals(this.kind, that.kind) &&
                Objects.equals(this.apiVersion, that.apiVersion) &&
                Objects.equals(this.metadata, that.metadata) &&
                Objects.equals(this.spec, that.spec);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata, spec);
    }
}
