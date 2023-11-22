/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                                                        @JsonProperty("results") List<ExecutionResult<T>> results) {

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
