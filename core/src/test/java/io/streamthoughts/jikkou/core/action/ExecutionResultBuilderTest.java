/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.action;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExecutionResultBuilderTest {

    @Test
    void shouldBuildExecutionResult() {

        ExecutionError error = new ExecutionError("error");
        ExecutionResult<HasMetadata> result = ExecutionResult
                .newBuilder()
                .status(ExecutionStatus.FAILED)
                .error(error)
                .error(error)
                .data(null)
                .build();

        GenericExecutionResult<HasMetadata> expected = new GenericExecutionResult<>(
                ExecutionStatus.FAILED,
                List.of(error, error),
                null
        );
        Assertions.assertEquals(expected, result);
    }
}