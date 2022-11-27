/*
 * Copyright 2022 StreamThoughts.
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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;


/**
 * 
 */
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata"
})
@Builder(builderMethodName = "builder", builderClassName = "Builder", toBuilder = true)
public class GenericResource implements HasMetadata {

    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    /**
     * Creates a new {@link GenericResource} instance.
     *
     * @param apiVersion    the apiVersion.
     * @param kind          the resource kind.
     * @param metadata      the resource metadata.
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata"
    })
    public GenericResource(final String apiVersion,
                           final String kind,
                           final ObjectMeta metadata) {
        this(apiVersion, kind, metadata, new LinkedHashMap<>());
    }

    public GenericResource(final String apiVersion,
                           final String kind,
                           final ObjectMeta metadata,
                           final Map<String, Object> additionalProperties) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.additionalProperties = additionalProperties;
    }


    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * (Required)
     */
    @Override
    public HasMetadata withMetadata(final ObjectMeta objectMeta) {
        return new GenericResource(apiVersion, kind, objectMeta);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


}
