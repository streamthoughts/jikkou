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
import io.streamthoughts.jikkou.api.model.HasConfigs;
import io.streamthoughts.jikkou.api.model.Nameable;
import java.beans.ConstructorProperties;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;


/**
 * KafkaTopicObject
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "name",
    "partitions",
    "replication_factor",
    "configs",
    "config_map_refs"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaTopicObject implements HasConfigs, Nameable
{

    @JsonProperty("name")
    private String name;
    @JsonProperty("partitions")
    private Integer partitions = -1;
    @JsonProperty("replication_factor")
    private Short replicationFactor = null;
    @JsonProperty("configs")
    private Configs configs;
    @JsonProperty("config_map_refs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @Singular
    private Set<String> configMapRefs = new LinkedHashSet<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTopicObject() {
    }

    /**
     * 
     * @param partitions
     * @param replicationFactor
     * @param configs
     * @param configMapRefs
     * @param name
     */
    @ConstructorProperties({
        "name",
        "partitions",
        "replicationFactor",
        "configs",
        "configMapRefs"
    })
    public V1KafkaTopicObject(String name, Integer partitions, Short replicationFactor, Configs configs, Set<String> configMapRefs) {
        super();
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
        this.configs = configs;
        this.configMapRefs = configMapRefs;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("partitions")
    public Integer getPartitions() {
        return partitions;
    }

    @JsonProperty("replication_factor")
    public Short getReplicationFactor() {
        return replicationFactor;
    }

    @JsonProperty("configs")
    public Configs getConfigs() {
        return configs;
    }

    @JsonProperty("config_map_refs")
    public Set<String> getConfigMapRefs() {
        return configMapRefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTopicObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("partitions");
        sb.append('=');
        sb.append(((this.partitions == null)?"<null>":this.partitions));
        sb.append(',');
        sb.append("replicationFactor");
        sb.append('=');
        sb.append(((this.replicationFactor == null)?"<null>":this.replicationFactor));
        sb.append(',');
        sb.append("configs");
        sb.append('=');
        sb.append(((this.configs == null)?"<null>":this.configs));
        sb.append(',');
        sb.append("configMapRefs");
        sb.append('=');
        sb.append(((this.configMapRefs == null)?"<null>":this.configMapRefs));
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
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.partitions == null)? 0 :this.partitions.hashCode()));
        result = ((result* 31)+((this.replicationFactor == null)? 0 :this.replicationFactor.hashCode()));
        result = ((result* 31)+((this.configs == null)? 0 :this.configs.hashCode()));
        result = ((result* 31)+((this.configMapRefs == null)? 0 :this.configMapRefs.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaTopicObject) == false) {
            return false;
        }
        V1KafkaTopicObject rhs = ((V1KafkaTopicObject) other);
        return ((((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.partitions == rhs.partitions)||((this.partitions!= null)&&this.partitions.equals(rhs.partitions))))&&((this.replicationFactor == rhs.replicationFactor)||((this.replicationFactor!= null)&&this.replicationFactor.equals(rhs.replicationFactor))))&&((this.configs == rhs.configs)||((this.configs!= null)&&this.configs.equals(rhs.configs))))&&((this.configMapRefs == rhs.configMapRefs)||((this.configMapRefs!= null)&&this.configMapRefs.equals(rhs.configMapRefs))));
    }

}
