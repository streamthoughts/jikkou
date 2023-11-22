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
package io.streamthoughts.jikkou.core.action;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a set of results for the execution of an action.
 *
 * @param <T> The type of the resource.
 */
public final class ExecutionResultSetBuilder<T extends HasMetadata> {

    private final List<ExecutionResult<T>> results = new ArrayList<>();

    ExecutionResultSetBuilder() {}

    /**
     * Adds the specified result to this builder.
     *
     * @param result The ExecutionResult.
     * @return {@code this}.s
     */
    public ExecutionResultSetBuilder<T> result(@NotNull ExecutionResult<T> result) {
        this.results.add(Objects.requireNonNull(result, "result cannot be null"));
        return this;
    }

    /**
     * Builds the ExecutionResultSet.
     *
     * @return The ExecutionResultSet.
     */
    public ExecutionResultSet<T> build() {
        return new GenericExecutionResultSet<>(results);
    }
}
