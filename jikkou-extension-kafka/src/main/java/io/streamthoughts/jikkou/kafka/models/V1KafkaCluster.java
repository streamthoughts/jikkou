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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasSpec;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ObjectTemplate;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;


/**
 * KafkaCluster
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "template",
    "spec"
})
@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("KafkaCluster")
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaCluster implements HasMetadata, HasSpec<V1KafkaClusterSpec>
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    private String apiVersion = "kafka.jikkou.io/v1beta2";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    private String kind = "KafkaCluster";
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    @JsonProperty("template")
    private ObjectTemplate template;
    /**
     * KafkaClusterSpec
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    @JsonPropertyDescription("")
    private io.streamthoughts.jikkou.kafka.models.V1KafkaClusterSpec spec;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaCluster() {
    }

    /**
     * 
     * @param template
     * @param metadata
     * @param apiVersion
     * @param kind
     * @param spec
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "template",
        "spec"
    })
    public V1KafkaCluster(String apiVersion, String kind, ObjectMeta metadata, ObjectTemplate template, io.streamthoughts.jikkou.kafka.models.V1KafkaClusterSpec spec) {
        super();
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.template = template;
        this.spec = spec;
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
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @JsonProperty("template")
    public ObjectTemplate getTemplate() {
        return template;
    }

    /**
     * KafkaClusterSpec
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    public io.streamthoughts.jikkou.kafka.models.V1KafkaClusterSpec getSpec() {
        return spec;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaCluster.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("apiVersion");
        sb.append('=');
        sb.append(((this.apiVersion == null)?"<null>":this.apiVersion));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
        sb.append(',');
        sb.append("template");
        sb.append('=');
        sb.append(((this.template == null)?"<null>":this.template));
        sb.append(',');
        sb.append("spec");
        sb.append('=');
        sb.append(((this.spec == null)?"<null>":this.spec));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.template == null)? 0 :this.template.hashCode()));
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.apiVersion == null)? 0 :this.apiVersion.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.spec == null)? 0 :this.spec.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaCluster) == false) {
            return false;
        }
        V1KafkaCluster rhs = ((V1KafkaCluster) other);
        return ((((((this.template == rhs.template)||((this.template!= null)&&this.template.equals(rhs.template)))&&((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata))))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.spec == rhs.spec)||((this.spec!= null)&&this.spec.equals(rhs.spec))));
    }

}
