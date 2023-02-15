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
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "principal",
    "roles",
    "permissions"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaAccessUserObject {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("principal")
    private String principal;
    @JsonProperty("roles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @Singular
    private Set<String> roles = new LinkedHashSet<String>();
    @JsonProperty("permissions")
    @Singular
    private List<V1KafkaAccessPermission> permissions = new ArrayList<V1KafkaAccessPermission>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaAccessUserObject() {
    }

    /**
     * 
     * @param principal
     * @param permissions
     * @param roles
     */
    @ConstructorProperties({
        "principal",
        "roles",
        "permissions"
    })
    public V1KafkaAccessUserObject(String principal, Set<String> roles, List<V1KafkaAccessPermission> permissions) {
        super();
        this.principal = principal;
        this.roles = roles;
        this.permissions = permissions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("principal")
    public String getPrincipal() {
        return principal;
    }

    @JsonProperty("roles")
    public Set<String> getRoles() {
        return roles;
    }

    @JsonProperty("permissions")
    public List<V1KafkaAccessPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaAccessUserObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("principal");
        sb.append('=');
        sb.append(((this.principal == null)?"<null>":this.principal));
        sb.append(',');
        sb.append("roles");
        sb.append('=');
        sb.append(((this.roles == null)?"<null>":this.roles));
        sb.append(',');
        sb.append("permissions");
        sb.append('=');
        sb.append(((this.permissions == null)?"<null>":this.permissions));
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
        result = ((result* 31)+((this.principal == null)? 0 :this.principal.hashCode()));
        result = ((result* 31)+((this.permissions == null)? 0 :this.permissions.hashCode()));
        result = ((result* 31)+((this.roles == null)? 0 :this.roles.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaAccessUserObject) == false) {
            return false;
        }
        V1KafkaAccessUserObject rhs = ((V1KafkaAccessUserObject) other);
        return ((((this.principal == rhs.principal)||((this.principal!= null)&&this.principal.equals(rhs.principal)))&&((this.permissions == rhs.permissions)||((this.permissions!= null)&&this.permissions.equals(rhs.permissions))))&&((this.roles == rhs.roles)||((this.roles!= null)&&this.roles.equals(rhs.roles))));
    }

}
