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
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;


/**
 * V1KafkaPrincipalAcl
 * <p>
 * KafkaPrincipalAcl object describes the list of allowed or denied operations for a Kafka resource.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("KafkaPrincipalAcl object describes the list of allowed or denied operations for a Kafka resource.")
@JsonPropertyOrder({
    "resource",
    "operations",
    "type",
    "host"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaPrincipalAcl {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("resource")
    @JsonPropertyDescription("")
    private V1KafkaResourceMatcher resource;
    /**
     * List of ACL operations e.g., ALL, READ, WRITE, CREATE, DELETE, etc.
     * (Required)
     * 
     */
    @JsonProperty("operations")
    @JsonPropertyDescription("List of ACL operations e.g., ALL, READ, WRITE, CREATE, DELETE, etc.")
    @Singular
    private List<AclOperation> operations = new ArrayList<AclOperation>();
    /**
     * The ACL permission type, i.e., ALLOW or DENY.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The ACL permission type, i.e., ALLOW or DENY.")
    @Builder.Default
    private AclPermissionType type = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    @Builder.Default
    private String host = "*";

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaPrincipalAcl() {
    }

    /**
     * 
     * @param operations
     * @param resource
     * @param host
     * @param type
     */
    @ConstructorProperties({
        "resource",
        "operations",
        "type",
        "host"
    })
    public V1KafkaPrincipalAcl(V1KafkaResourceMatcher resource, List<AclOperation> operations, AclPermissionType type, String host) {
        super();
        this.resource = resource;
        this.operations = operations;
        this.type = type;
        this.host = host;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("resource")
    public V1KafkaResourceMatcher getResource() {
        return resource;
    }

    /**
     * List of ACL operations e.g., ALL, READ, WRITE, CREATE, DELETE, etc.
     * (Required)
     * 
     */
    @JsonProperty("operations")
    public List<AclOperation> getOperations() {
        return operations;
    }

    /**
     * The ACL permission type, i.e., ALLOW or DENY.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public AclPermissionType getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaPrincipalAcl.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("resource");
        sb.append('=');
        sb.append(((this.resource == null)?"<null>":this.resource));
        sb.append(',');
        sb.append("operations");
        sb.append('=');
        sb.append(((this.operations == null)?"<null>":this.operations));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("host");
        sb.append('=');
        sb.append(((this.host == null)?"<null>":this.host));
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
        result = ((result* 31)+((this.operations == null)? 0 :this.operations.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.resource == null)? 0 :this.resource.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaPrincipalAcl) == false) {
            return false;
        }
        V1KafkaPrincipalAcl rhs = ((V1KafkaPrincipalAcl) other);
        return (((((this.host == rhs.host)||((this.host!= null)&&this.host.equals(rhs.host)))&&((this.operations == rhs.operations)||((this.operations!= null)&&this.operations.equals(rhs.operations))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.resource == rhs.resource)||((this.resource!= null)&&this.resource.equals(rhs.resource))));
    }

}
