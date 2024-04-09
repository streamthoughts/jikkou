/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaReference;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.Modes;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "compatibilityLevel",
    "mode",
    "schemaRegistry",
    "schemaType",
    "schema",
    "references"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1SchemaRegistrySubjectSpec {

    /**
     * The schema compatibility level for this subject.
     * 
     */
    @JsonProperty("compatibilityLevel")
    @JsonPropertyDescription("The schema compatibility level for this subject.")
    private CompatibilityLevels compatibilityLevel;
    /**
     * The mode for this subject: IMPORT, READONLY, READWRITE.
     * 
     */
    @JsonProperty("mode")
    @JsonPropertyDescription("The mode for this subject: IMPORT, READONLY, READWRITE.")
    private Modes mode;
    @JsonProperty("schemaRegistry")
    private SchemaRegistry schemaRegistry;
    /**
     * The schema format: AVRO, PROTOBUF, JSON.
     * 
     */
    @JsonProperty("schemaType")
    @JsonPropertyDescription("The schema format: AVRO, PROTOBUF, JSON.")
    private SchemaType schemaType;
    @JsonProperty("schema")
    private SchemaHandle schema;
    /**
     * Specifies the names of referenced schemas (Optional).
     * 
     */
    @JsonProperty("references")
    @JsonPropertyDescription("Specifies the names of referenced schemas (Optional).")
    @Singular
    private List<SubjectSchemaReference> references = new ArrayList<SubjectSchemaReference>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1SchemaRegistrySubjectSpec() {
    }

    /**
     * 
     * @param mode
     * @param schema
     * @param schemaRegistry
     * @param references
     * @param compatibilityLevel
     * @param schemaType
     */
    @ConstructorProperties({
        "compatibilityLevel",
        "mode",
        "schemaRegistry",
        "schemaType",
        "schema",
        "references"
    })
    public V1SchemaRegistrySubjectSpec(CompatibilityLevels compatibilityLevel, Modes mode, SchemaRegistry schemaRegistry, SchemaType schemaType, SchemaHandle schema, List<SubjectSchemaReference> references) {
        super();
        this.compatibilityLevel = compatibilityLevel;
        this.mode = mode;
        this.schemaRegistry = schemaRegistry;
        this.schemaType = schemaType;
        this.schema = schema;
        this.references = references;
    }

    /**
     * The schema compatibility level for this subject.
     * 
     */
    @JsonProperty("compatibilityLevel")
    public CompatibilityLevels getCompatibilityLevel() {
        return compatibilityLevel;
    }

    /**
     * The mode for this subject: IMPORT, READONLY, READWRITE.
     * 
     */
    @JsonProperty("mode")
    public Modes getMode() {
        return mode;
    }

    @JsonProperty("schemaRegistry")
    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }

    /**
     * The schema format: AVRO, PROTOBUF, JSON.
     * 
     */
    @JsonProperty("schemaType")
    public SchemaType getSchemaType() {
        return schemaType;
    }

    @JsonProperty("schema")
    public SchemaHandle getSchema() {
        return schema;
    }

    /**
     * Specifies the names of referenced schemas (Optional).
     * 
     */
    @JsonProperty("references")
    public List<SubjectSchemaReference> getReferences() {
        return references;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1SchemaRegistrySubjectSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("compatibilityLevel");
        sb.append('=');
        sb.append(((this.compatibilityLevel == null)?"<null>":this.compatibilityLevel));
        sb.append(',');
        sb.append("mode");
        sb.append('=');
        sb.append(((this.mode == null)?"<null>":this.mode));
        sb.append(',');
        sb.append("schemaRegistry");
        sb.append('=');
        sb.append(((this.schemaRegistry == null)?"<null>":this.schemaRegistry));
        sb.append(',');
        sb.append("schemaType");
        sb.append('=');
        sb.append(((this.schemaType == null)?"<null>":this.schemaType));
        sb.append(',');
        sb.append("schema");
        sb.append('=');
        sb.append(((this.schema == null)?"<null>":this.schema));
        sb.append(',');
        sb.append("references");
        sb.append('=');
        sb.append(((this.references == null)?"<null>":this.references));
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
        result = ((result* 31)+((this.mode == null)? 0 :this.mode.hashCode()));
        result = ((result* 31)+((this.schemaType == null)? 0 :this.schemaType.hashCode()));
        result = ((result* 31)+((this.schema == null)? 0 :this.schema.hashCode()));
        result = ((result* 31)+((this.schemaRegistry == null)? 0 :this.schemaRegistry.hashCode()));
        result = ((result* 31)+((this.references == null)? 0 :this.references.hashCode()));
        result = ((result* 31)+((this.compatibilityLevel == null)? 0 :this.compatibilityLevel.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1SchemaRegistrySubjectSpec) == false) {
            return false;
        }
        V1SchemaRegistrySubjectSpec rhs = ((V1SchemaRegistrySubjectSpec) other);
        return (((((((this.mode == rhs.mode)||((this.mode!= null)&&this.mode.equals(rhs.mode)))&&((this.schemaType == rhs.schemaType)||((this.schemaType!= null)&&this.schemaType.equals(rhs.schemaType))))&&((this.schema == rhs.schema)||((this.schema!= null)&&this.schema.equals(rhs.schema))))&&((this.schemaRegistry == rhs.schemaRegistry)||((this.schemaRegistry!= null)&&this.schemaRegistry.equals(rhs.schemaRegistry))))&&((this.references == rhs.references)||((this.references!= null)&&this.references.equals(rhs.references))))&&((this.compatibilityLevel == rhs.compatibilityLevel)||((this.compatibilityLevel!= null)&&this.compatibilityLevel.equals(rhs.compatibilityLevel))));
    }

}
