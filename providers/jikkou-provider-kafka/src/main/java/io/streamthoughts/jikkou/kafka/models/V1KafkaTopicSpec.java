/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasConfigRefs;
import java.beans.ConstructorProperties;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "partitions",
    "replicas",
    "configs",
    "configMapRefs"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaTopicSpec implements HasConfigRefs
{

    @JsonProperty("partitions")
    @Builder.Default
    private Integer partitions = -1;
    @JsonProperty("replicas")
    @Builder.Default
    private Short replicas = null;
    @JsonProperty("configs")
    private Configs configs;
    @JsonProperty("configMapRefs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @Singular
    private Set<String> configMapRefs = new LinkedHashSet<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTopicSpec() {
    }

    /**
     * 
     * @param partitions
     * @param configs
     * @param configMapRefs
     * @param replicas
     */
    @ConstructorProperties({
        "partitions",
        "replicas",
        "configs",
        "configMapRefs"
    })
    public V1KafkaTopicSpec(Integer partitions, Short replicas, Configs configs, Set<String> configMapRefs) {
        super();
        this.partitions = partitions;
        this.replicas = replicas;
        this.configs = configs;
        this.configMapRefs = configMapRefs;
    }

    @JsonProperty("partitions")
    public Integer getPartitions() {
        return partitions;
    }

    @JsonProperty("replicas")
    public Short getReplicas() {
        return replicas;
    }

    @JsonProperty("configs")
    public Configs getConfigs() {
        return configs;
    }

    @JsonProperty("configMapRefs")
    public Set<String> getConfigMapRefs() {
        return configMapRefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTopicSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("partitions");
        sb.append('=');
        sb.append(((this.partitions == null)?"<null>":this.partitions));
        sb.append(',');
        sb.append("replicas");
        sb.append('=');
        sb.append(((this.replicas == null)?"<null>":this.replicas));
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
        result = ((result* 31)+((this.partitions == null)? 0 :this.partitions.hashCode()));
        result = ((result* 31)+((this.configs == null)? 0 :this.configs.hashCode()));
        result = ((result* 31)+((this.configMapRefs == null)? 0 :this.configMapRefs.hashCode()));
        result = ((result* 31)+((this.replicas == null)? 0 :this.replicas.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaTopicSpec) == false) {
            return false;
        }
        V1KafkaTopicSpec rhs = ((V1KafkaTopicSpec) other);
        return (((((this.partitions == rhs.partitions)||((this.partitions!= null)&&this.partitions.equals(rhs.partitions)))&&((this.configs == rhs.configs)||((this.configs!= null)&&this.configs.equals(rhs.configs))))&&((this.configMapRefs == rhs.configMapRefs)||((this.configMapRefs!= null)&&this.configMapRefs.equals(rhs.configMapRefs))))&&((this.replicas == rhs.replicas)||((this.replicas!= null)&&this.replicas.equals(rhs.replicas))));
    }

}
