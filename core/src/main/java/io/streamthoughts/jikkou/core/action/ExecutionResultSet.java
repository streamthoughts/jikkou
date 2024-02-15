/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.action;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;

/**
 * Represents a set of results for the execution of an action.
 *
 * @param <T> The type of the resource.
 */
@Reflectable
@JsonDeserialize(as = GenericExecutionResultSet.class)
public interface ExecutionResultSet<T extends HasMetadata> {

    /**
     * Gets all the execution results.
     *
     * @return The list of {@link ExecutionResult}.
     */
    List<ExecutionResult<T>> results();

    /**
     * Creates a new {@link ExecutionResultSetBuilder}.
     *
     * @return The ExecutionResultSetBuilder.
     * @param <T> The type of the resource.
     */
    static <T extends HasMetadata> ExecutionResultSetBuilder<T> newBuilder() {
        return new ExecutionResultSetBuilder<>();
    }
}
