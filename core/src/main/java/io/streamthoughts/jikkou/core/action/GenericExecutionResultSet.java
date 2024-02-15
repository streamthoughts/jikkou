/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;

@JsonPropertyOrder({
        "results"
})
public record GenericExecutionResultSet<T extends HasMetadata>(@JsonProperty("results") List<ExecutionResult<T>> results)
        implements ExecutionResultSet<T> {


    @ConstructorProperties({
            "results"
    })
    public GenericExecutionResultSet {
        results = Collections.unmodifiableList(results);
    }
    
}
