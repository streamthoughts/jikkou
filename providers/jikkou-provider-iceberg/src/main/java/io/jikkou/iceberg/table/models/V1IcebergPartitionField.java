/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.Reflectable;
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
@JsonPropertyOrder({"sourceColumn", "transform", "name"})
@Jacksonized
@Reflectable
public class V1IcebergPartitionField {

    /**
     * Name of the source column to partition on.
     */
    @JsonProperty("sourceColumn")
    @JsonPropertyDescription("Name of the source column to partition on.")
    private String sourceColumn;

    /**
     * Partition transform: identity, year, month, day, hour, bucket[N], truncate[W], void.
     */
    @JsonProperty("transform")
    @JsonPropertyDescription("Partition transform: identity, year, month, day, hour, bucket[N], truncate[W], void.")
    private String transform;

    /**
     * Optional custom name for the partition field (defaults to transform(sourceColumn)).
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Optional custom name for the partition field (defaults to transform(sourceColumn)).")
    private String name;
}
