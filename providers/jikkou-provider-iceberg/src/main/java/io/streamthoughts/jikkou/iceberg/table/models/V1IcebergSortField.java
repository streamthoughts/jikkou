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
@JsonPropertyOrder({"column", "term", "direction", "nullOrder"})
@Reflectable
public class V1IcebergSortField {

    /**
     * Column name to sort by (mutually exclusive with 'term').
     */
    @JsonProperty("column")
    @JsonPropertyDescription("Column name to sort by (mutually exclusive with 'term').")
    private String column;

    /**
     * Expression-based sort term, e.g. 'bucket[16](user_id)' (mutually exclusive with 'column').
     */
    @JsonProperty("term")
    @JsonPropertyDescription("Expression-based sort term, e.g. 'bucket[16](user_id)' (mutually exclusive with 'column').")
    private String term;

    /**
     * Sort direction: asc or desc.
     */
    @JsonProperty("direction")
    @JsonPropertyDescription("Sort direction: asc or desc.")
    private String direction = "asc";

    /**
     * Null ordering: first or last.
     */
    @JsonProperty("nullOrder")
    @JsonPropertyDescription("Null ordering: first or last.")
    private String nullOrder = "last";

    public V1IcebergSortField() {
    }

    public V1IcebergSortField(String column, String term, String direction, String nullOrder) {
        this.column = column;
        this.term = term;
        this.direction = direction;
        this.nullOrder = nullOrder;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNullOrder() {
        return nullOrder;
    }

    public void setNullOrder(String nullOrder) {
        this.nullOrder = nullOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1IcebergSortField that = (V1IcebergSortField) o;
        return Objects.equals(column, that.column)
            && Objects.equals(term, that.term)
            && Objects.equals(direction, that.direction)
            && Objects.equals(nullOrder, that.nullOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, term, direction, nullOrder);
    }

    @Override
    public String toString() {
        return "V1IcebergSortField{"
            + "column='" + column + '\''
            + ", term='" + term + '\''
            + ", direction='" + direction + '\''
            + ", nullOrder='" + nullOrder + '\''
            + '}';
    }
}
