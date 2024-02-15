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
 * Represents the result of the execution of an action.
 *
 * @param <T> The type of the resource.
 */
@Reflectable
@JsonDeserialize(as = GenericExecutionResult.class)
public interface ExecutionResult<T extends HasMetadata> {

    /**
     * Gets the execution status.
     *
     * @return The status
     */
    ExecutionStatus status();

    /**
     * Gets the list of execution errors. This method should return an empty list if no errors occurred
     * during the action. e.g., {@link #status()} is returning {@link ExecutionStatus#SUCCEEDED}.
     *
     * @return The execution errors.
     */
    List<ExecutionError> errors();

    /**
     * Gets the data.
     *
     * @return The data.
     */
    T data();

    /**
     * Creates a new {@link ExecutionResultBuilder}.
     *
     * @param <T> The type of the resource.
     * @return The ExecutionResultBuilder.
     */
    static <T extends HasMetadata> ExecutionResultBuilder<T> newBuilder() {
        return new ExecutionResultBuilder<>();
    }
}
