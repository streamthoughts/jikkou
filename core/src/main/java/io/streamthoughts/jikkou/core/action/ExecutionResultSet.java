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
