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
@JsonPropertyOrder({"identifierFields", "columns"})
@Jacksonized
@Reflectable
public class V1IcebergSchema {

    /**
     * Names of the primary-key (identifier) fields for MERGE/UPSERT semantics.
     */
    @JsonProperty("identifierFields")
    @JsonPropertyDescription("Names of the primary-key (identifier) fields for MERGE/UPSERT semantics.")
    @Singular
    private List<String> identifierFields;

    /**
     * Ordered list of columns (fields) in this schema.
     */
    @JsonProperty("columns")
    @JsonPropertyDescription("Ordered list of columns (fields) in this schema.")
    @Singular
    private List<V1IcebergColumn> columns;
}
