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
import java.util.List;

@JsonPropertyOrder({
        "status",
        "errors",
        "data"
})
public record GenericExecutionResult<T extends HasMetadata>(@JsonProperty("status") ExecutionStatus status,
                                                            @JsonProperty("errors") List<ExecutionError> errors,
                                                            @JsonProperty("data") T data) implements ExecutionResult<T> {


    @ConstructorProperties({
            "status",
            "errors",
            "data"
    })
    public GenericExecutionResult {
    }

}
