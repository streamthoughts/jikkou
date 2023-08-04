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
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.annotation.ExtensionName;
import io.streamthoughts.jikkou.annotation.Priority;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTransformationDecoratorTest {

    @Test
    void shouldReturnNamePassedToDecorator() {
        // Given
        ResourceTransformationDecorator<HasMetadata> object = new ResourceTransformationDecorator<>(new TestTransformation())
                .withName("foo");
        // When
        String result = object.getName();
        // Then
        Assertions.assertEquals("foo", result);
    }


    @Test
    void shouldReturnPriorityPassedToDecorator() {
        // Given
        ResourceTransformationDecorator<HasMetadata> object = new ResourceTransformationDecorator<>(new TestTransformation())
                .withPriority(HasPriority.NO_ORDER);
        // When
        int result = object.getPriority();
        // Then
        Assertions.assertEquals(HasPriority.NO_ORDER, result);
    }


    @Priority(100)
    @ExtensionName("test")
    private static class TestTransformation implements ResourceTransformation<HasMetadata> {
        @Override
        public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform, @NotNull HasItems resources) {
            return Optional.empty();
        }
    }
}