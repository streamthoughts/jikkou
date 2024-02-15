/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.Configs;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Names(singular = "kafkabroker", plural = "kafkabrokers", shortNames = {
    "kb"
})
@Verbs({

})
@JsonPropertyOrder({
    "id",
    "host",
    "port",
    "rack",
    "configs"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaBrokersSpec {

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
    @JsonProperty("configs")
    private Configs configs;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaBrokersSpec() {
    }

    /**
     * 
     * @param configs
     * @param rack
     * @param port
     * @param host
     * @param id
     */
    @ConstructorProperties({
        "id",
        "host",
        "port",
        "rack",
        "configs"
    })
    public V1KafkaBrokersSpec(String id, String host, Integer port, String rack, Configs configs) {
        super();
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.configs = configs;
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

    @JsonProperty("configs")
    public Configs getConfigs() {
        return configs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaBrokersSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("configs");
        sb.append('=');
        sb.append(((this.configs == null)?"<null>":this.configs));
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
        result = ((result* 31)+((this.configs == null)? 0 :this.configs.hashCode()));
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
        if ((other instanceof V1KafkaBrokersSpec) == false) {
            return false;
        }
        V1KafkaBrokersSpec rhs = ((V1KafkaBrokersSpec) other);
        return ((((((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host)))&&((this.configs == rhs.configs)||((this.configs!= null)&&this.configs.equals(rhs.configs))))&&((this.rack == rhs.rack)||((this.rack!= null)&&this.rack.equals(rhs.rack))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.port == rhs.port)||((this.port!= null)&&this.port.equals(rhs.port))));
    }

}
