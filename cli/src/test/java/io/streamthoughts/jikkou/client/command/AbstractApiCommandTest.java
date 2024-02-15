/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractApiCommandTest {

    @Test
    void shouldAccumulateListOptions() {
        // Given
        TestApiCommand command = new TestApiCommand();
        ApiOptionSpec options = new ApiOptionSpec(
                "options",
                "",
                List.class,
                null,
                false
        );
        // When
        command.option(options, "A");
        command.option(options, "B");
        command.option(options, "C");

        // Then
        Assertions.assertEquals(Map.of("options", List.of("A", "B", "C")), command.options());


    }

    public static class TestApiCommand extends AbstractApiCommand {

        @Override
        public Integer call() throws Exception {
            return null;
        }
    }
}