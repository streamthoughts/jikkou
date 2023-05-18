/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.api.annotations.ExtensionName;
import io.streamthoughts.jikkou.api.annotations.Priority;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceValidationDecoratorTest {

    @Test
    void shouldReturnNamePassedToDecorator() {
        // Given
        ResourceValidationDecorator<HasMetadata> object = new ResourceValidationDecorator<>(new TestValidation())
                .withName("foo");
        // When
        String result = object.getName();
        // Then
        Assertions.assertEquals("foo", result);
    }


    @Test
    void shouldReturnPriorityPassedToDecorator() {
        // Given
        ResourceValidationDecorator<HasMetadata> object = new ResourceValidationDecorator<>(new TestValidation())
                .withPriority(HasPriority.NO_ORDER);
        // When
        int result = object.getPriority();
        // Then
        Assertions.assertEquals(HasPriority.NO_ORDER, result);
    }


    @Priority(100)
    @ExtensionName("test")
    private static class TestValidation implements ResourceValidation<HasMetadata> { }
}