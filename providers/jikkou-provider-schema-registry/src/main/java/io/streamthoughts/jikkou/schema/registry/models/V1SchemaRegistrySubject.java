/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.Verb;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * SchemaRegistrySubject
 * <p>
 * The SchemaRegistrySubject resource allows managing subjects and schema on a Schema Registry.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("The SchemaRegistrySubject resource allows managing subjects and schema on a Schema Registry.")
@Names(singular = "schemaregistrysubject", plural = "schemaregistrysubjects", shortNames = {
    "sr"
})
@Verbs({
    Verb.LIST,
    Verb.CREATE,
    Verb.UPDATE,
    Verb.DELETE,
    Verb.GET,
    Verb.APPLY
})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec"
})
@ApiVersion("schemaregistry.jikkou.io/v1beta2")
@Kind("SchemaRegistrySubject")
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1SchemaRegistrySubject implements HasMetadata, HasSpec<V1SchemaRegistrySubjectSpec> , Resource
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    @Builder.Default
    private String apiVersion = "schemaregistry.jikkou.io/v1beta2";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @Builder.Default
    private String kind = "SchemaRegistrySubject";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    private io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec spec;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1SchemaRegistrySubject() {
    }

    /**
     * 
     * @param metadata
     * @param apiVersion
     * @param kind
     * @param spec
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "spec"
    })
    public V1SchemaRegistrySubject(String apiVersion, String kind, ObjectMeta metadata, io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec spec) {
        super();
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("spec")
    public io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec getSpec() {
        return spec;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1SchemaRegistrySubject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("spec");
        sb.append('=');
        sb.append(((this.spec == null)?"<null>":this.spec));
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
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.spec == null)? 0 :this.spec.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1SchemaRegistrySubject) == false) {
            return false;
        }
        V1SchemaRegistrySubject rhs = ((V1SchemaRegistrySubject) other);
        return (((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.spec == rhs.spec)||((this.spec!= null)&&this.spec.equals(rhs.spec))));
    }

}
