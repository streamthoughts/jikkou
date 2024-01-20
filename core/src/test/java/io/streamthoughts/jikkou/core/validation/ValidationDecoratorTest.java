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