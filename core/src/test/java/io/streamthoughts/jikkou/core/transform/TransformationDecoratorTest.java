/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TransformationDecoratorTest {

    @Test
    void shouldReturnNamePassedToDecorator() {
        // Given
        TransformationDecorator<HasMetadata> object = new TransformationDecorator<>(new TestTransformation())
                .name("foo");
        // When
        String result = object.getName();
        // Then
        Assertions.assertEquals("foo", result);
    }


    @Test
    void shouldReturnPriorityPassedToDecorator() {
        // Given
        TransformationDecorator<HasMetadata> object = new TransformationDecorator<>(new TestTransformation())
                .priority(HasPriority.NO_ORDER);
        // When
        int result = object.getPriority();
        // Then
        Assertions.assertEquals(HasPriority.NO_ORDER, result);
    }


    @Priority(100)
    @Description("test")
    private static class TestTransformation implements Transformation<HasMetadata> {
        @Override
        public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                        @NotNull HasItems resources,
                                                        @NotNull ReconciliationContext context) {
            return Optional.empty();
        }
    }
}