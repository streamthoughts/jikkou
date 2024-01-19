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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * V1KafkaConsumerGroupMember
 * <p>
 * Detailed description of a single group instance in the cluster.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Detailed description of a single group instance in the cluster.")
@JsonPropertyOrder({
    "memberId",
    "groupInstanceId",
    "clientId",
    "host",
    "assignments"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaConsumerGroupMember {

    /**
     * The member ID.
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    @JsonPropertyDescription("The member ID.")
    private String memberId;
    /**
     * The group instance ID.
     * 
     */
    @JsonProperty("groupInstanceId")
    @JsonPropertyDescription("The group instance ID.")
    private String groupInstanceId;
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
    public V1KafkaConsumerGroupMember() {
    }

    /**
     * 
     * @param groupInstanceId
     * @param clientId
     * @param assignments
     * @param host
     * @param memberId
     */
    @ConstructorProperties({
        "memberId",
        "groupInstanceId",
        "clientId",
        "host",
        "assignments"
    })
    public V1KafkaConsumerGroupMember(String memberId, String groupInstanceId, String clientId, String host, List<String> assignments) {
        super();
        this.memberId = memberId;
        this.groupInstanceId = groupInstanceId;
        this.clientId = clientId;
        this.host = host;
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
     * The group instance ID.
     * 
     */
    @JsonProperty("groupInstanceId")
    public String getGroupInstanceId() {
        return groupInstanceId;
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
        sb.append(V1KafkaConsumerGroupMember.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("memberId");
        sb.append('=');
        sb.append(((this.memberId == null)?"<null>":this.memberId));
        sb.append(',');
        sb.append("groupInstanceId");
        sb.append('=');
        sb.append(((this.groupInstanceId == null)?"<null>":this.groupInstanceId));
        sb.append(',');
        sb.append("clientId");
        sb.append('=');
        sb.append(((this.clientId == null)?"<null>":this.clientId));
        sb.append(',');
        sb.append("host");
        sb.append('=');
        sb.append(((this.host == null)?"<null>":this.host));
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
        result = ((result* 31)+((this.groupInstanceId == null)? 0 :this.groupInstanceId.hashCode()));
        result = ((result* 31)+((this.host == null)? 0 :this.host.hashCode()));
        result = ((result* 31)+((this.clientId == null)? 0 :this.clientId.hashCode()));
        result = ((result* 31)+((this.assignments == null)? 0 :this.assignments.hashCode()));
        result = ((result* 31)+((this.memberId == null)? 0 :this.memberId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaConsumerGroupMember) == false) {
            return false;
        }
        V1KafkaConsumerGroupMember rhs = ((V1KafkaConsumerGroupMember) other);
        return ((((((this.groupInstanceId == rhs.groupInstanceId)||((this.groupInstanceId!= null)&&this.groupInstanceId.equals(rhs.groupInstanceId)))&&((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host))))&&((this.clientId == rhs.clientId)||((this.clientId!= null)&&this.clientId.equals(rhs.clientId))))&&((this.assignments == rhs.assignments)||((this.assignments!= null)&&this.assignments.equals(rhs.assignments))))&&((this.memberId == rhs.memberId)||((this.memberId!= null)&&this.memberId.equals(rhs.memberId))));
    }

}
