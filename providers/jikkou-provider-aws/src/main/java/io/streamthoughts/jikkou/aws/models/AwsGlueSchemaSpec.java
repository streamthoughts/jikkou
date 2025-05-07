/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.aws.model.Compatibility;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
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
    "compatibility",
    "dataFormat",
    "schemaDefinition",
    "description"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class AwsGlueSchemaSpec {

    /**
     * The data format applies to all versions of a Glue schema.
     * 
     */
    @JsonProperty("compatibility")
    @JsonPropertyDescription("The data format applies to all versions of a Glue schema.")
    private Compatibility compatibility;
    /**
     * The data format applies to all versions of the Glue schema: AVRO, PROTOBUF, JSON.
     * 
     */
    @JsonProperty("dataFormat")
    @JsonPropertyDescription("The data format applies to all versions of the Glue schema: AVRO, PROTOBUF, JSON.")
    private SchemaType dataFormat;
    @JsonProperty("schemaDefinition")
    private SchemaHandle schemaDefinition;
    /**
     * Provides description about the schema.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Provides description about the schema.")
    private Object description;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AwsGlueSchemaSpec() {
    }

    /**
     * 
     * @param dataFormat
     * @param schemaDefinition
     * @param description
     * @param compatibility
     */
    @ConstructorProperties({
        "compatibility",
        "dataFormat",
        "schemaDefinition",
        "description"
    })
    public AwsGlueSchemaSpec(Compatibility compatibility, SchemaType dataFormat, SchemaHandle schemaDefinition, Object description) {
        super();
        this.compatibility = compatibility;
        this.dataFormat = dataFormat;
        this.schemaDefinition = schemaDefinition;
        this.description = description;
    }

    /**
     * The data format applies to all versions of a Glue schema.
     * 
     */
    @JsonProperty("compatibility")
    public Compatibility getCompatibility() {
        return compatibility;
    }

    /**
     * The data format applies to all versions of the Glue schema: AVRO, PROTOBUF, JSON.
     * 
     */
    @JsonProperty("dataFormat")
    public SchemaType getDataFormat() {
        return dataFormat;
    }

    @JsonProperty("schemaDefinition")
    public SchemaHandle getSchemaDefinition() {
        return schemaDefinition;
    }

    /**
     * Provides description about the schema.
     * 
     */
    @JsonProperty("description")
    public Object getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AwsGlueSchemaSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("compatibility");
        sb.append('=');
        sb.append(((this.compatibility == null)?"<null>":this.compatibility));
        sb.append(',');
        sb.append("dataFormat");
        sb.append('=');
        sb.append(((this.dataFormat == null)?"<null>":this.dataFormat));
        sb.append(',');
        sb.append("schemaDefinition");
        sb.append('=');
        sb.append(((this.schemaDefinition == null)?"<null>":this.schemaDefinition));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
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
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.compatibility == null)? 0 :this.compatibility.hashCode()));
        result = ((result* 31)+((this.dataFormat == null)? 0 :this.dataFormat.hashCode()));
        result = ((result* 31)+((this.schemaDefinition == null)? 0 :this.schemaDefinition.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AwsGlueSchemaSpec) == false) {
            return false;
        }
        AwsGlueSchemaSpec rhs = ((AwsGlueSchemaSpec) other);
        return (((((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description)))&&((this.compatibility == rhs.compatibility)||((this.compatibility!= null)&&this.compatibility.equals(rhs.compatibility))))&&((this.dataFormat == rhs.dataFormat)||((this.dataFormat!= null)&&this.dataFormat.equals(rhs.dataFormat))))&&((this.schemaDefinition == rhs.schemaDefinition)||((this.schemaDefinition!= null)&&this.schemaDefinition.equals(rhs.schemaDefinition))));
    }

}
