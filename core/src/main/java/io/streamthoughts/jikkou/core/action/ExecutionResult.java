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
