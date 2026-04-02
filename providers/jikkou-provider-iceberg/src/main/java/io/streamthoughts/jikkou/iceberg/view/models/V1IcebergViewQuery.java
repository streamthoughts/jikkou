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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@JsonPropertyOrder({"sql", "dialect"})
@Jacksonized
@Reflectable
public class V1IcebergViewQuery {

    /**
     * The SQL SELECT statement defining the view for this dialect.
     */
    @JsonProperty("sql")
    @JsonPropertyDescription("The SQL SELECT statement defining the view for this dialect.")
    private String sql;

    /**
     * The SQL dialect (e.g. 'spark', 'trino', 'presto', 'hive').
     */
    @JsonProperty("dialect")
    @JsonPropertyDescription("The SQL dialect (e.g. 'spark', 'trino', 'presto', 'hive').")
    private String dialect;
}
