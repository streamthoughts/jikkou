/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.view.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergSchema;
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
@JsonPropertyOrder({"schema", "queries", "defaultNamespace", "defaultCatalog", "properties"})
@Jacksonized
@Reflectable
public class V1IcebergViewSpec {

    /**
     * The output schema of the view (read-only, populated on collect, inferred by the engine).
     */
    @JsonProperty("schema")
    @JsonPropertyDescription("The output schema of the view (read-only, populated on collect, inferred by the engine).")
    private V1IcebergSchema schema;

    /**
     * SQL query definitions, one per dialect. At least one query is required.
     */
    @JsonProperty("queries")
    @JsonPropertyDescription("SQL query definitions, one per dialect. At least one query is required.")
    @Singular
    private List<V1IcebergViewQuery> queries;

    /**
     * Default namespace for resolving unqualified table references in the SQL.
     */
    @JsonProperty("defaultNamespace")
    @JsonPropertyDescription("Default namespace for resolving unqualified table references in the SQL.")
    private String defaultNamespace;

    /**
     * Default catalog for resolving unqualified table references in the SQL.
     */
    @JsonProperty("defaultCatalog")
    @JsonPropertyDescription("Default catalog for resolving unqualified table references in the SQL.")
    private String defaultCatalog;

    /**
     * View-level properties (key-value pairs stored in the catalog).
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("View-level properties (key-value pairs stored in the catalog).")
    private Map<String, String> properties;
}
