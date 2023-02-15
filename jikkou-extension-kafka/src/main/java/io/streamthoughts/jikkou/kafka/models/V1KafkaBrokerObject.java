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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.api.model.Configs;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;


/**
 * KafkaBrokerObject
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "id",
    "host",
    "port",
    "rack",
    "configs"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaBrokerObject {

    @JsonProperty("id")
    private String id;
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("rack")
    private String rack;
    @JsonProperty("configs")
    private Configs configs;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaBrokerObject() {
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
    public V1KafkaBrokerObject(String id, String host, Integer port, String rack, Configs configs) {
        super();
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.configs = configs;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

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
        sb.append(V1KafkaBrokerObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof V1KafkaBrokerObject) == false) {
            return false;
        }
        V1KafkaBrokerObject rhs = ((V1KafkaBrokerObject) other);
        return ((((((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host)))&&((this.configs == rhs.configs)||((this.configs!= null)&&this.configs.equals(rhs.configs))))&&((this.rack == rhs.rack)||((this.rack!= null)&&this.rack.equals(rhs.rack))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.port == rhs.port)||((this.port!= null)&&this.port.equals(rhs.port))));
    }

}
