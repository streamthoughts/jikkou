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
import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * ApiActionResult.
 *
 * @param kind       The object Kind.
 * @param apiVersion The API version.
 * @param metadata   The object metadata.
 * @param results    The ExecutionResults.
 * @param <T>        The type of the embedded resource.
 */
@Kind(ApiActionResultSet.KIND)
@ApiVersion(ApiActionResultSet.API_VERSION)
@JsonPropertyOrder({"kind", "apiVersion", "metadata", "results"})
@Reflectable
@JsonDeserialize
public record ApiActionResultSet<T extends HasMetadata>(@JsonProperty("kind") String kind,
                                                        @JsonProperty("apiVersion") String apiVersion,
                                                        @JsonProperty("metadata") ObjectMeta metadata,
                                                        @JsonProperty("results") List<ExecutionResult<T>> results) implements Resource {

    public static final String KIND = "ApiActionResultSet";
    public static final String API_VERSION = "core.jikkou.io/v1";

    @ConstructorProperties({"kind", "apiVersion", "metadata", "result"})
    public ApiActionResultSet {
    }

    /**
     * Creates a new {@link ApiActionResultSet} instance.
     *
     * @param metadata The metadata.
     * @param results  The ExecutionResults
     */
    public ApiActionResultSet(@NotNull ObjectMeta metadata,
                              @NotNull List<ExecutionResult<T>> results) {
        this(KIND, API_VERSION, metadata, results);
    }
}
