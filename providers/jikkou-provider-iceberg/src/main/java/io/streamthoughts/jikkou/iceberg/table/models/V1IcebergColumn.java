/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.table.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "type", "required", "doc", "default", "writeDefault", "previousName"})
@Reflectable
public class V1IcebergColumn {

    /**
     * The column name.
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The column name.")
    private String name;

    /**
     * The column type: a primitive type string (e.g. 'string', 'long', 'decimal(10,2)')
     * or a nested complex-type object (struct/list/map).
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The column type: a primitive type string (e.g. 'string', 'long', 'decimal(10,2)') or a nested complex-type object (struct/list/map).")
    private Object type;

    /**
     * Whether the column is required (non-nullable). Defaults to false.
     */
    @JsonProperty("required")
    @JsonPropertyDescription("Whether the column is required (non-nullable). Defaults to false.")
    private Boolean required;

    /**
     * Optional documentation string for this column.
     */
    @JsonProperty("doc")
    @JsonPropertyDescription("Optional documentation string for this column.")
    private String doc;

    /**
     * Initial default value set at column creation time (immutable after creation).
     */
    @JsonProperty("default")
    @JsonPropertyDescription("Initial default value set at column creation time (immutable after creation).")
    private Object defaultValue;

    /**
     * Write-default value used by writers when the column value is absent (updatable independently).
     */
    @JsonProperty("writeDefault")
    @JsonPropertyDescription("Write-default value used by writers when the column value is absent (updatable independently).")
    private Object writeDefault;

    /**
     * Previous column name — triggers a safe rename (preserves field ID) rather than drop+add.
     */
    @JsonProperty("previousName")
    @JsonPropertyDescription("Previous column name \u2014 triggers a safe rename (preserves field ID) rather than drop+add.")
    private String previousName;

    /**
     * Creates a new {@code V1IcebergColumn} instance.
     */
    public V1IcebergColumn() {
        this.required = false;
    }

    /**
     * Creates a new {@code V1IcebergColumn} instance.
     */
    public V1IcebergColumn(String name,
                           Object type,
                           Boolean required,
                           String doc,
                           Object defaultValue,
                           Object writeDefault,
                           String previousName) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.doc = doc;
        this.defaultValue = defaultValue;
        this.writeDefault = writeDefault;
        this.previousName = previousName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    /**
     * Returns the initial default value.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getWriteDefault() {
        return writeDefault;
    }

    public void setWriteDefault(Object writeDefault) {
        this.writeDefault = writeDefault;
    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1IcebergColumn that)) return false;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(required, that.required)
                && Objects.equals(doc, that.doc)
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(writeDefault, that.writeDefault)
                && Objects.equals(previousName, that.previousName);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, type, required, doc, defaultValue, writeDefault, previousName);
    }
}
