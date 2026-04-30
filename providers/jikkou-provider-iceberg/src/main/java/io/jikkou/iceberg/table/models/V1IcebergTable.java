/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.annotation.Names;
import io.jikkou.core.annotation.ReconciliationOrder;
import io.jikkou.core.annotation.Reflectable;
import io.jikkou.core.annotation.Verbs;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasSpec;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.Resource;
import io.jikkou.core.models.Verb;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

/**
 * Manage tables in an Apache Iceberg catalog.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Getter
@EqualsAndHashCode
@Description("Manage tables in an Apache Iceberg catalog.")
@Names(singular = "icebergtable", plural = "icebergtables", local = "tables", shortNames = {"itb"})
@Verbs({Verb.LIST, Verb.CREATE, Verb.UPDATE, Verb.DELETE, Verb.GET, Verb.APPLY})
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "spec"})
@ApiVersion("iceberg.jikkou.io/v1beta1")
@Kind("IcebergTable")
@ReconciliationOrder(200)
@Jacksonized
@Reflectable
public class V1IcebergTable implements HasMetadata, HasSpec<V1IcebergTableSpec>, Resource {

    @JsonProperty("apiVersion")
    @Builder.Default
    private final String apiVersion = "iceberg.jikkou.io/v1beta1";

    @JsonProperty("kind")
    @Builder.Default
    private final String kind = "IcebergTable";

    @JsonProperty("metadata")
    private final ObjectMeta metadata;

    @JsonProperty("spec")
    private final V1IcebergTableSpec spec;
}
