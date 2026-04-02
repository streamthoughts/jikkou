/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.view.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.Verb;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

/**
 * Manage views in an Apache Iceberg catalog.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Getter
@EqualsAndHashCode
@Description("Manage views in an Apache Iceberg catalog.")
@Names(singular = "icebergview", plural = "icebergviews", shortNames = {"ivw"})
@Verbs({Verb.LIST, Verb.CREATE, Verb.UPDATE, Verb.DELETE, Verb.GET, Verb.APPLY})
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "spec"})
@ApiVersion("iceberg.jikkou.io/v1beta1")
@Kind("IcebergView")
@Jacksonized
@Reflectable
public class V1IcebergView implements HasMetadata, HasSpec<V1IcebergViewSpec>, Resource {

    @JsonProperty("apiVersion")
    @Builder.Default
    private final String apiVersion = "iceberg.jikkou.io/v1beta1";

    @JsonProperty("kind")
    @Builder.Default
    private final String kind = "IcebergView";

    @JsonProperty("metadata")
    private final ObjectMeta metadata;

    @JsonProperty("spec")
    private final V1IcebergViewSpec spec;
}
