/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import java.util.List;
import java.util.Optional;

/**
 * ApiChangeResultList.
 */
@Kind(ApiChangeResultList.KIND)
@ApiVersion(ApiChangeResultList.API_VERSION)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "metadata",
        "dryRun",
        "results"
})
@Reflectable
@JsonDeserialize
public record ApiChangeResultList(@JsonProperty("kind") String kind,
                                  @JsonProperty("apiVersion") String apiVersion,
                                  @JsonProperty("metadata") ObjectMeta metadata,
                                  @JsonProperty("dryRun") boolean dryRun,
                                  @JsonProperty("results") List<ChangeResult> results
) {
    public static final String KIND = "ApiChangeResultList";
    public static final String API_VERSION = "core.jikkou.io/v1";


    /**
     * Creates a new {@link ApiChangeResultList} instance.
     *
     * @param dryRun  specify whether teh reconciliation have benn executed in dry-run.
     * @param results list of change result.
     */
    public ApiChangeResultList(boolean dryRun, List<ChangeResult> results) {
        this(dryRun, new ObjectMeta(), results);
    }

    /**
     * Creates a new {@link ApiChangeResultList} instance.
     *
     * @param dryRun  specify whether the reconciliation have benn executed in dry-run.
     * @param results list of change result.
     */
    public ApiChangeResultList(boolean dryRun,
                               ObjectMeta metadata,
                               List<ChangeResult> results) {
        this(
                KIND,
                API_VERSION,
                metadata,
                dryRun,
                results
        );
    }

    @JsonProperty("metadata")
    @Override
    public ObjectMeta metadata() {
        ObjectMeta objectMeta = Optional.ofNullable(metadata).orElse(new ObjectMeta());
        return objectMeta.toBuilder()
                .withAnnotation(CoreAnnotations.JIKKOU_IO_CHANGE_COUNT, results.size())
                .build();
    }
}
