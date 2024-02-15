/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationDecoratorTest {

    @Test
    void shouldReturnNamePassedToDecorator() {
        // Given
        ValidationDecorator<HasMetadata> object = new ValidationDecorator<>(new TestValidation())
                .name("foo");
        // When
        String result = object.getName();
        // Then
        Assertions.assertEquals("foo", result);
    }


    @Test
    void shouldReturnPriorityPassedToDecorator() {
        // Given
        ValidationDecorator<HasMetadata> object = new ValidationDecorator<>(new TestValidation())
                .priority(HasPriority.NO_ORDER);
        // When
        int result = object.getPriority();
        // Then
        Assertions.assertEquals(HasPriority.NO_ORDER, result);
    }


    @Priority(100)
    @Description("test")
    private static class TestValidation implements Validation<HasMetadata> { }
}