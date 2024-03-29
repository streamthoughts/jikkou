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
    "vendor"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class SchemaRegistry {

    /**
     * Provides information about the Schema Registry used to store the describe schema.
     * 
     */
    @JsonProperty("vendor")
    @JsonPropertyDescription("Provides information about the Schema Registry used to store the describe schema.")
    private String vendor;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SchemaRegistry() {
    }

    /**
     * 
     * @param vendor
     */
    @ConstructorProperties({
        "vendor"
    })
    public SchemaRegistry(String vendor) {
        super();
        this.vendor = vendor;
    }

    /**
     * Provides information about the Schema Registry used to store the describe schema.
     * 
     */
    @JsonProperty("vendor")
    public String getVendor() {
        return vendor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SchemaRegistry.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("vendor");
        sb.append('=');
        sb.append(((this.vendor == null)?"<null>":this.vendor));
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
        result = ((result* 31)+((this.vendor == null)? 0 :this.vendor.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaRegistry) == false) {
            return false;
        }
        SchemaRegistry rhs = ((SchemaRegistry) other);
        return ((this.vendor == rhs.vendor)||((this.vendor!= null)&&this.vendor.equals(rhs.vendor)));
    }

}
