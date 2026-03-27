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
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@JsonPropertyOrder({"location", "schema", "partitionFields", "sortFields", "properties"})
@Jacksonized
@Reflectable
public class V1IcebergTableSpec {

    /**
     * The table base location in the storage system (e.g. s3://bucket/path).
     */
    @JsonProperty("location")
    @JsonPropertyDescription("The table base location in the storage system (e.g. s3://bucket/path).")
    private String location;

    @JsonProperty("schema")
    private V1IcebergSchema schema;

    /**
     * Partition fields defining how the table data is partitioned.
     */
    @JsonProperty("partitionFields")
    @JsonPropertyDescription("Partition fields defining how the table data is partitioned.")
    @Singular
    private List<V1IcebergPartitionField> partitionFields;

    /**
     * Default sort order applied when writing data to this table.
     */
    @JsonProperty("sortFields")
    @JsonPropertyDescription("Default sort order applied when writing data to this table.")
    @Singular
    private List<V1IcebergSortField> sortFields;

    /**
     * Table-level properties (key-value pairs stored in the catalog).
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Table-level properties (key-value pairs stored in the catalog).")
    private Map<String, String> properties;
}
