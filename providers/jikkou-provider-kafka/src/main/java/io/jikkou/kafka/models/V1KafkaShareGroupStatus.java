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
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * V1KafkaShareGroupStatus
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "state",
    "members",
    "offsets",
    "coordinator"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaShareGroupStatus {

    /**
     * The share group state.
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("The share group state.")
    private String state;
    /**
     * List of share group members.
     * 
     */
    @JsonProperty("members")
    @JsonPropertyDescription("List of share group members.")
    @Singular
    private List<V1KafkaShareGroupMember> members = new ArrayList<V1KafkaShareGroupMember>();
    /**
     * List of share-partition start offsets.
     * 
     */
    @JsonProperty("offsets")
    @JsonPropertyDescription("List of share-partition start offsets.")
    @Singular
    private List<V1KafkaConsumerOffset> offsets = new ArrayList<V1KafkaConsumerOffset>();
    /**
     * V1KafkaNode
     * <p>
     * Information about a Kafka node.
     * 
     */
    @JsonProperty("coordinator")
    @JsonPropertyDescription("Information about a Kafka node.")
    private V1KafkaNode coordinator;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaShareGroupStatus() {
    }

    /**
     * 
     * @param coordinator
     * @param offsets
     * @param members
     * @param state
     */
    @ConstructorProperties({
        "state",
        "members",
        "offsets",
        "coordinator"
    })
    public V1KafkaShareGroupStatus(String state, List<V1KafkaShareGroupMember> members, List<V1KafkaConsumerOffset> offsets, V1KafkaNode coordinator) {
        super();
        this.state = state;
        this.members = members;
        this.offsets = offsets;
        this.coordinator = coordinator;
    }

    /**
     * The share group state.
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * List of share group members.
     * 
     */
    @JsonProperty("members")
    public List<V1KafkaShareGroupMember> getMembers() {
        return members;
    }

    /**
     * List of share-partition start offsets.
     * 
     */
    @JsonProperty("offsets")
    public List<V1KafkaConsumerOffset> getOffsets() {
        return offsets;
    }

    /**
     * V1KafkaNode
     * <p>
     * Information about a Kafka node.
     * 
     */
    @JsonProperty("coordinator")
    public V1KafkaNode getCoordinator() {
        return coordinator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaShareGroupStatus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("members");
        sb.append('=');
        sb.append(((this.members == null)?"<null>":this.members));
        sb.append(',');
        sb.append("offsets");
        sb.append('=');
        sb.append(((this.offsets == null)?"<null>":this.offsets));
        sb.append(',');
        sb.append("coordinator");
        sb.append('=');
        sb.append(((this.coordinator == null)?"<null>":this.coordinator));
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
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.coordinator == null)? 0 :this.coordinator.hashCode()));
        result = ((result* 31)+((this.offsets == null)? 0 :this.offsets.hashCode()));
        result = ((result* 31)+((this.members == null)? 0 :this.members.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaShareGroupStatus) == false) {
            return false;
        }
        V1KafkaShareGroupStatus rhs = ((V1KafkaShareGroupStatus) other);
        return (((((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state)))&&((this.coordinator == rhs.coordinator)||((this.coordinator!= null)&&this.coordinator.equals(rhs.coordinator))))&&((this.offsets == rhs.offsets)||((this.offsets!= null)&&this.offsets.equals(rhs.offsets))))&&((this.members == rhs.members)||((this.members!= null)&&this.members.equals(rhs.members))));
    }

}
