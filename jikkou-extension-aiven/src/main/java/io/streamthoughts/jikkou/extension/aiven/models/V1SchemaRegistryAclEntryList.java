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
package io.streamthoughts.jikkou.extension.aiven.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Description;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import io.streamthoughts.jikkou.api.model.annotations.Names;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;


/**
 * List of ACL entries for Schema Registry
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("List of ACL entries for Schema Registry")
@Names(singular = "avn-schema-registry-acl-list", shortNames = {
    "avnsral"
})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@ApiVersion("kafka.aiven.io/v1beta1")
@Kind("SchemaRegistryAclEntryList")
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1SchemaRegistryAclEntryList implements ResourceListObject<V1SchemaRegistryAclEntry>
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    private String apiVersion = "kafka.aiven.io/v1beta1";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    private String kind = "SchemaRegistryAclEntryList";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    @JsonProperty("items")
    @Singular
    private List<io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry> items = new ArrayList<io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1SchemaRegistryAclEntryList() {
    }

    /**
     * 
     * @param metadata
     * @param apiVersion
     * @param kind
     * @param items
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "items"
    })
    public V1SchemaRegistryAclEntryList(String apiVersion, String kind, ObjectMeta metadata, List<io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry> items) {
        super();
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.items = items;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @JsonProperty("items")
    public List<io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry> getItems() {
        return items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1SchemaRegistryAclEntryList.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("apiVersion");
        sb.append('=');
        sb.append(((this.apiVersion == null)?"<null>":this.apiVersion));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
        sb.append(',');
        sb.append("items");
        sb.append('=');
        sb.append(((this.items == null)?"<null>":this.items));
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
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.apiVersion == null)? 0 :this.apiVersion.hashCode()));
        result = ((result* 31)+((this.items == null)? 0 :this.items.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1SchemaRegistryAclEntryList) == false) {
            return false;
        }
        V1SchemaRegistryAclEntryList rhs = ((V1SchemaRegistryAclEntryList) other);
        return (((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.items == rhs.items)||((this.items!= null)&&this.items.equals(rhs.items))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))));
    }

}
