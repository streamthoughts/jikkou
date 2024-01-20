/*
 * Copyright 2024 The original authors
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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * V1KafkaNode
 * <p>
 * Information about a Kafka node.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Information about a Kafka node.")
@JsonPropertyOrder({
    "id",
    "host",
    "port",
    "rack"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaNode {

    /**
     * The node id of this node.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The node id of this node.")
    private String id;
    /**
     * The host name for this node.
     * 
     */
    @JsonProperty("host")
    @JsonPropertyDescription("The host name for this node.")
    private String host;
    /**
     * The port for this node.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("The port for this node.")
    private Integer port;
    /**
     * The rack for this node (null if this node has no defined rack).
     * 
     */
    @JsonProperty("rack")
    @JsonPropertyDescription("The rack for this node (null if this node has no defined rack).")
    private String rack;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaNode() {
    }

    /**
     * 
     * @param rack
     * @param port
     * @param host
     * @param id
     */
    @ConstructorProperties({
        "id",
        "host",
        "port",
        "rack"
    })
    public V1KafkaNode(String id, String host, Integer port, String rack) {
        super();
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
    }

    /**
     * The node id of this node.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * The host name for this node.
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * The port for this node.
     * 
     */
    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    /**
     * The rack for this node (null if this node has no defined rack).
     * 
     */
    @JsonProperty("rack")
    public String getRack() {
        return rack;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaNode.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("host");
        sb.append('=');
        sb.append(((this.host == null)?"<null>":this.host));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null)?"<null>":this.port));
        sb.append(',');
        sb.append("rack");
        sb.append('=');
        sb.append(((this.rack == null)?"<null>":this.rack));
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
        result = ((result* 31)+((this.host == null)? 0 :this.host.hashCode()));
        result = ((result* 31)+((this.rack == null)? 0 :this.rack.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.port == null)? 0 :this.port.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaNode) == false) {
            return false;
        }
        V1KafkaNode rhs = ((V1KafkaNode) other);
        return (((((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host)))&&((this.rack == rhs.rack)||((this.rack!= null)&&this.rack.equals(rhs.rack))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.port == rhs.port)||((this.port!= null)&&this.port.equals(rhs.port))));
    }

}
