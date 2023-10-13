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
package io.streamthoughts.jikkou.kafka.connect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.annotation.ApiVersion;
import io.streamthoughts.jikkou.annotation.Description;
import io.streamthoughts.jikkou.annotation.Kind;
import io.streamthoughts.jikkou.annotation.Names;
import io.streamthoughts.jikkou.annotation.Reflectable;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasSpec;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.Resource;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * V1KafkaConnector
 * <p>
 * The KafkaConnector resource allows creating and managing connectors in a Kafka Connect cluster.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("The KafkaConnector resource allows creating and managing connectors in a Kafka Connect cluster.")
@Names(singular = "kafkaconnector", plural = "kafkaconnectors", shortNames = {
    "kc"
})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec",
    "status"
})
@ApiVersion("kafka.jikkou.io/v1beta1")
@Kind("KafkaConnector")
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaConnector implements HasMetadata, HasSpec<V1KafkaConnectorSpec> , Resource
{

    /**
     * The version of specification format for that particular resource.
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    @JsonPropertyDescription("The version of specification format for that particular resource.")
    @Builder.Default
    private String apiVersion = "kafka.jikkou.io/v1beta1";
    /**
     * The kind of that resource.
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @JsonPropertyDescription("The kind of that resource.")
    @Builder.Default
    private String kind = "KafkaConnector";
    /**
     * ObjectMeta
     * <p>
     * Metadata attached to the resources.
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata attached to the resources.")
    private ObjectMeta metadata;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    private io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec spec;
    @JsonProperty("status")
    private V1KafkaConnectorStatus status;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaConnector() {
    }

    /**
     * 
     * @param metadata
     * @param apiVersion
     * @param kind
     * @param spec
     * @param status
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "spec",
        "status"
    })
    public V1KafkaConnector(String apiVersion, String kind, ObjectMeta metadata, io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec spec, V1KafkaConnectorStatus status) {
        super();
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
        this.status = status;
    }

    /**
     * The version of specification format for that particular resource.
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * The kind of that resource.
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    /**
     * ObjectMeta
     * <p>
     * Metadata attached to the resources.
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    public io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec getSpec() {
        return spec;
    }

    @JsonProperty("status")
    public V1KafkaConnectorStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaConnector.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("spec");
        sb.append('=');
        sb.append(((this.spec == null)?"<null>":this.spec));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
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
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.apiVersion == null)? 0 :this.apiVersion.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.spec == null)? 0 :this.spec.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaConnector) == false) {
            return false;
        }
        V1KafkaConnector rhs = ((V1KafkaConnector) other);
        return ((((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.spec == rhs.spec)||((this.spec!= null)&&this.spec.equals(rhs.spec))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

}
