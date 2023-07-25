/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Description;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import java.beans.ConstructorProperties;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@With
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@Description("")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "change"
})
@ApiVersion("core.jikkou.io/v1beta2")
@Kind("GenericResourceChange")
public class GenericResourceChange<T> implements HasMetadataChange<ValueChange<T>> {

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;
    private final ValueChange<T> change;


    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "change"
    })
    public GenericResourceChange(final String kind,
                                 final String apiVersion,
                                 final ObjectMeta metadata,
                                 final ValueChange<T> change) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.metadata = metadata;
        this.change = change;
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
        return Optional.ofNullable(metadata).orElse(new ObjectMeta());
    }

    /**
     * {@inheritDoc}
     **/
    @JsonProperty("change")
    @Override
    public ValueChange<T> getChange() {
        return change;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericResourceChange<?> that = (GenericResourceChange<?>) o;
        return Objects.equals(kind, that.kind) && Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(change, that.change);
    }
    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata, change);
    }
}
