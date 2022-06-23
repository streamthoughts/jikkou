/*
 * Copyright 2022 StreamThoughts.
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
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;


/**
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "roles",
    "users"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaAuthorizationSpec {

    @JsonProperty("roles")
    @Singular
    private List<V1KafkaAccessRoleObject> roles = new ArrayList<V1KafkaAccessRoleObject>();
    @JsonProperty("users")
    @Singular
    private List<V1KafkaAccessUserObject> users = new ArrayList<V1KafkaAccessUserObject>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaAuthorizationSpec() {
    }

    /**
     * 
     * @param roles
     * @param users
     */
    @ConstructorProperties({
        "roles",
        "users"
    })
    public V1KafkaAuthorizationSpec(List<V1KafkaAccessRoleObject> roles, List<V1KafkaAccessUserObject> users) {
        super();
        this.roles = roles;
        this.users = users;
    }

    @JsonProperty("roles")
    public List<V1KafkaAccessRoleObject> getRoles() {
        return roles;
    }

    @JsonProperty("users")
    public List<V1KafkaAccessUserObject> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaAuthorizationSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("roles");
        sb.append('=');
        sb.append(((this.roles == null)?"<null>":this.roles));
        sb.append(',');
        sb.append("users");
        sb.append('=');
        sb.append(((this.users == null)?"<null>":this.users));
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
        result = ((result* 31)+((this.users == null)? 0 :this.users.hashCode()));
        result = ((result* 31)+((this.roles == null)? 0 :this.roles.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaAuthorizationSpec) == false) {
            return false;
        }
        V1KafkaAuthorizationSpec rhs = ((V1KafkaAuthorizationSpec) other);
        return (((this.users == rhs.users)||((this.users!= null)&&this.users.equals(rhs.users)))&&((this.roles == rhs.roles)||((this.roles!= null)&&this.roles.equals(rhs.roles))));
    }

}
