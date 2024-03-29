/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "permission",
    "username",
    "topic"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaTopicAclEntrySpec {

    /**
     * Kafka permission
     * (Required)
     * 
     */
    @JsonProperty("permission")
    @JsonPropertyDescription("Kafka permission")
    private Permission permission;
    /**
     * Username
     * (Required)
     * 
     */
    @JsonProperty("username")
    @JsonPropertyDescription("Username")
    private String username;
    /**
     * Topic name pattern
     * (Required)
     * 
     */
    @JsonProperty("topic")
    @JsonPropertyDescription("Topic name pattern")
    private String topic;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTopicAclEntrySpec() {
    }

    /**
     * 
     * @param topic
     * @param permission
     * @param username
     */
    @ConstructorProperties({
        "permission",
        "username",
        "topic"
    })
    public V1KafkaTopicAclEntrySpec(Permission permission, String username, String topic) {
        super();
        this.permission = permission;
        this.username = username;
        this.topic = topic;
    }

    /**
     * Kafka permission
     * (Required)
     * 
     */
    @JsonProperty("permission")
    public Permission getPermission() {
        return permission;
    }

    /**
     * Username
     * (Required)
     * 
     */
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    /**
     * Topic name pattern
     * (Required)
     * 
     */
    @JsonProperty("topic")
    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTopicAclEntrySpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("permission");
        sb.append('=');
        sb.append(((this.permission == null)?"<null>":this.permission));
        sb.append(',');
        sb.append("username");
        sb.append('=');
        sb.append(((this.username == null)?"<null>":this.username));
        sb.append(',');
        sb.append("topic");
        sb.append('=');
        sb.append(((this.topic == null)?"<null>":this.topic));
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
        result = ((result* 31)+((this.topic == null)? 0 :this.topic.hashCode()));
        result = ((result* 31)+((this.permission == null)? 0 :this.permission.hashCode()));
        result = ((result* 31)+((this.username == null)? 0 :this.username.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaTopicAclEntrySpec) == false) {
            return false;
        }
        V1KafkaTopicAclEntrySpec rhs = ((V1KafkaTopicAclEntrySpec) other);
        return ((((this.topic == rhs.topic)||((this.topic!= null)&&this.topic.equals(rhs.topic)))&&((this.permission == rhs.permission)||((this.permission!= null)&&this.permission.equals(rhs.permission))))&&((this.username == rhs.username)||((this.username!= null)&&this.username.equals(rhs.username))));
    }

}
