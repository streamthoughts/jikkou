/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.Description;
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
 * V1KafkaShareGroupMember
 * <p>
 * Detailed description of a single share group member.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Detailed description of a single share group member.")
@JsonClassDescription("Detailed description of a single share group member.")
@JsonPropertyOrder({
    "memberId",
    "clientId",
    "host",
    "rackId",
    "assignments"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaShareGroupMember {

    /**
     * The member ID.
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    @JsonPropertyDescription("The member ID.")
    private String memberId;
    /**
     * The client ID.
     * 
     */
    @JsonProperty("clientId")
    @JsonPropertyDescription("The client ID.")
    private String clientId;
    /**
     * The member host.
     * 
     */
    @JsonProperty("host")
    @JsonPropertyDescription("The member host.")
    private String host;
    /**
     * The rack ID of the member.
     * 
     */
    @JsonProperty("rackId")
    @JsonPropertyDescription("The rack ID of the member.")
    private String rackId;
    /**
     * List of topic-partitions assigned to the member.
     * 
     */
    @JsonProperty("assignments")
    @JsonPropertyDescription("List of topic-partitions assigned to the member.")
    @Singular
    private List<String> assignments = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaShareGroupMember() {
    }

    /**
     * 
     * @param clientId
     * @param assignments
     * @param host
     * @param memberId
     * @param rackId
     */
    @ConstructorProperties({
        "memberId",
        "clientId",
        "host",
        "rackId",
        "assignments"
    })
    public V1KafkaShareGroupMember(String memberId, String clientId, String host, String rackId, List<String> assignments) {
        super();
        this.memberId = memberId;
        this.clientId = clientId;
        this.host = host;
        this.rackId = rackId;
        this.assignments = assignments;
    }

    /**
     * The member ID.
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    public String getMemberId() {
        return memberId;
    }

    /**
     * The client ID.
     * 
     */
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * The member host.
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * The rack ID of the member.
     * 
     */
    @JsonProperty("rackId")
    public String getRackId() {
        return rackId;
    }

    /**
     * List of topic-partitions assigned to the member.
     * 
     */
    @JsonProperty("assignments")
    public List<String> getAssignments() {
        return assignments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaShareGroupMember.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("memberId");
        sb.append('=');
        sb.append(((this.memberId == null)?"<null>":this.memberId));
        sb.append(',');
        sb.append("clientId");
        sb.append('=');
        sb.append(((this.clientId == null)?"<null>":this.clientId));
        sb.append(',');
        sb.append("host");
        sb.append('=');
        sb.append(((this.host == null)?"<null>":this.host));
        sb.append(',');
        sb.append("rackId");
        sb.append('=');
        sb.append(((this.rackId == null)?"<null>":this.rackId));
        sb.append(',');
        sb.append("assignments");
        sb.append('=');
        sb.append(((this.assignments == null)?"<null>":this.assignments));
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
        result = ((result* 31)+((this.clientId == null)? 0 :this.clientId.hashCode()));
        result = ((result* 31)+((this.assignments == null)? 0 :this.assignments.hashCode()));
        result = ((result* 31)+((this.memberId == null)? 0 :this.memberId.hashCode()));
        result = ((result* 31)+((this.rackId == null)? 0 :this.rackId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaShareGroupMember) == false) {
            return false;
        }
        V1KafkaShareGroupMember rhs = ((V1KafkaShareGroupMember) other);
        return ((((((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host)))&&((this.clientId == rhs.clientId)||((this.clientId!= null)&&this.clientId.equals(rhs.clientId))))&&((this.assignments == rhs.assignments)||((this.assignments!= null)&&this.assignments.equals(rhs.assignments))))&&((this.memberId == rhs.memberId)||((this.memberId!= null)&&this.memberId.equals(rhs.memberId))))&&((this.rackId == rhs.rackId)||((this.rackId!= null)&&this.rackId.equals(rhs.rackId))));
    }

}
