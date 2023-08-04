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
package io.streamthoughts.jikkou.api.error;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ValidationExceptionTest {

    @Test
    void shouldGetFlattenAllExceptions() {
        // Given
        ValidationException exception = new ValidationException(
                List.of(
                        new ValidationException(List.of(
                                new ValidationException("ErrorA", "A"),
                                new ValidationException("ErrorB", "B"))),
                        new ValidationException("ErrorC", "C")
                )
        );

        // When
        List<ValidationException> results = exception.asList();

        // Then
        Assertions.assertEquals(3, results.size());
    }
}