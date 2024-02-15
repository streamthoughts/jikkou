/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
     * Adds the specified results to this builder.
     *
     * @param results The list of ExecutionResult.
     * @return {@code this}.s
     */
    public ExecutionResultSetBuilder<T> results(@NotNull List<ExecutionResult<T>> results) {
        this.results.addAll(Objects.requireNonNull(results, "results cannot be null"));
        return this;
    }


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
