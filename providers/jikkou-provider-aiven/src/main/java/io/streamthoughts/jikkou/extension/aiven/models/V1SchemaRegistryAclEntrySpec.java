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
    "resource"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1SchemaRegistryAclEntrySpec {

    /**
     * ACL entry for Schema Registry
     * (Required)
     * 
     */
    @JsonProperty("permission")
    @JsonPropertyDescription("ACL entry for Schema Registry")
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
     * Schema Registry ACL entry resource name pattern
     * (Required)
     * 
     */
    @JsonProperty("resource")
    @JsonPropertyDescription("Schema Registry ACL entry resource name pattern")
    private String resource;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1SchemaRegistryAclEntrySpec() {
    }

    /**
     * 
     * @param resource
     * @param permission
     * @param username
     */
    @ConstructorProperties({
        "permission",
        "username",
        "resource"
    })
    public V1SchemaRegistryAclEntrySpec(Permission permission, String username, String resource) {
        super();
        this.permission = permission;
        this.username = username;
        this.resource = resource;
    }

    /**
     * ACL entry for Schema Registry
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
     * Schema Registry ACL entry resource name pattern
     * (Required)
     * 
     */
    @JsonProperty("resource")
    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1SchemaRegistryAclEntrySpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("permission");
        sb.append('=');
        sb.append(((this.permission == null)?"<null>":this.permission));
        sb.append(',');
        sb.append("username");
        sb.append('=');
        sb.append(((this.username == null)?"<null>":this.username));
        sb.append(',');
        sb.append("resource");
        sb.append('=');
        sb.append(((this.resource == null)?"<null>":this.resource));
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
        result = ((result* 31)+((this.resource == null)? 0 :this.resource.hashCode()));
        result = ((result* 31)+((this.permission == null)? 0 :this.permission.hashCode()));
        result = ((result* 31)+((this.username == null)? 0 :this.username.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1SchemaRegistryAclEntrySpec) == false) {
            return false;
        }
        V1SchemaRegistryAclEntrySpec rhs = ((V1SchemaRegistryAclEntrySpec) other);
        return ((((this.resource == rhs.resource)||((this.resource!= null)&&this.resource.equals(rhs.resource)))&&((this.permission == rhs.permission)||((this.permission!= null)&&this.permission.equals(rhs.permission))))&&((this.username == rhs.username)||((this.username!= null)&&this.username.equals(rhs.username))));
    }

}
