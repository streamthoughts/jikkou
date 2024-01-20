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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "leader",
    "isr",
    "replicas"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class KafkaTopicPartitionInfo {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("leader")
    private Integer leader;
    @JsonProperty("isr")
    @Singular
    private List<Integer> isr = new ArrayList<Integer>();
    @JsonProperty("replicas")
    @Singular
    private List<Integer> replicas = new ArrayList<Integer>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public KafkaTopicPartitionInfo() {
    }

    /**
     * 
     * @param leader
     * @param replicas
     * @param isr
     * @param id
     */
    @ConstructorProperties({
        "id",
        "leader",
        "isr",
        "replicas"
    })
    public KafkaTopicPartitionInfo(Integer id, Integer leader, List<Integer> isr, List<Integer> replicas) {
        super();
        this.id = id;
        this.leader = leader;
        this.isr = isr;
        this.replicas = replicas;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("leader")
    public Integer getLeader() {
        return leader;
    }

    @JsonProperty("isr")
    public List<Integer> getIsr() {
        return isr;
    }

    @JsonProperty("replicas")
    public List<Integer> getReplicas() {
        return replicas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(KafkaTopicPartitionInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("leader");
        sb.append('=');
        sb.append(((this.leader == null)?"<null>":this.leader));
        sb.append(',');
        sb.append("isr");
        sb.append('=');
        sb.append(((this.isr == null)?"<null>":this.isr));
        sb.append(',');
        sb.append("replicas");
        sb.append('=');
        sb.append(((this.replicas == null)?"<null>":this.replicas));
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
        result = ((result* 31)+((this.leader == null)? 0 :this.leader.hashCode()));
        result = ((result* 31)+((this.isr == null)? 0 :this.isr.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.replicas == null)? 0 :this.replicas.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof KafkaTopicPartitionInfo) == false) {
            return false;
        }
        KafkaTopicPartitionInfo rhs = ((KafkaTopicPartitionInfo) other);
        return (((((this.leader == rhs.leader)||((this.leader!= null)&&this.leader.equals(rhs.leader)))&&((this.isr == rhs.isr)||((this.isr!= null)&&this.isr.equals(rhs.isr))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.replicas == rhs.replicas)||((this.replicas!= null)&&this.replicas.equals(rhs.replicas))));
    }

}
