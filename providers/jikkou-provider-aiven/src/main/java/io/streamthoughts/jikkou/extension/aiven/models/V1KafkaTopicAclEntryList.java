/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * List of ACL entries for Kafka Topic
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("List of ACL entries for Kafka Topic")
@Names(singular = "avn-kafka-topic-acl-list", shortNames = {
    "avnktal"
})
@Verbs({

})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@ApiVersion("kafka.aiven.io/v1beta1")
@Kind("KafkaTopicAclEntryList")
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaTopicAclEntryList implements ResourceList<V1KafkaTopicAclEntry>
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    @Builder.Default
    private String apiVersion = "kafka.aiven.io/v1beta1";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @Builder.Default
    private String kind = "KafkaTopicAclEntryList";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    @JsonProperty("items")
    @Singular
    private List<io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry> items = new ArrayList<io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTopicAclEntryList() {
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
    public V1KafkaTopicAclEntryList(String apiVersion, String kind, ObjectMeta metadata, List<io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry> items) {
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
    public List<io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry> getItems() {
        return items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTopicAclEntryList.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof V1KafkaTopicAclEntryList) == false) {
            return false;
        }
        V1KafkaTopicAclEntryList rhs = ((V1KafkaTopicAclEntryList) other);
        return (((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.items == rhs.items)||((this.items!= null)&&this.items.equals(rhs.items))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))));
    }

}
