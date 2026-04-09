/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.Reflectable;
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

    /**
     * The partition id.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The partition id.")
    private Integer id;
    /**
     * The id of the broker that is the leader for this partition.
     * 
     */
    @JsonProperty("leader")
    @JsonPropertyDescription("The id of the broker that is the leader for this partition.")
    private Integer leader;
    /**
     * The list of in-sync replica ids for this partition.
     * 
     */
    @JsonProperty("isr")
    @JsonPropertyDescription("The list of in-sync replica ids for this partition.")
    @Singular
    private List<Integer> isr = new ArrayList<Integer>();
    /**
     * The list of replica ids for this partition.
     * 
     */
    @JsonProperty("replicas")
    @JsonPropertyDescription("The list of replica ids for this partition.")
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

    /**
     * The partition id.
     * 
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * The id of the broker that is the leader for this partition.
     * 
     */
    @JsonProperty("leader")
    public Integer getLeader() {
        return leader;
    }

    /**
     * The list of in-sync replica ids for this partition.
     * 
     */
    @JsonProperty("isr")
    public List<Integer> getIsr() {
        return isr;
    }

    /**
     * The list of replica ids for this partition.
     * 
     */
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
