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
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionSelectorFactoryTest {


    @Test
    void shouldReturnFieldSelectorGivenExpressionWithNoSelectorName() {
        // Given
        ExpressionSelectorFactory factory = new ExpressionSelectorFactory();
        String expression = "metadata.labels.env IN (production, staging)";

        // When
        List<Selector> selectors = factory.make(List.of(expression));

        // Then
        Assertions.assertEquals(1, selectors.size());
        Assertions.assertInstanceOf(FieldSelector.class, selectors.get(0));
        Assertions.assertEquals(List.of(expression), selectors.get(0).getSelectorExpressions());
    }

    @Test
    void shouldThrowExceptionGivenExpressionWithInvalidSelectorName() {
        // Given
        ExpressionSelectorFactory factory = new ExpressionSelectorFactory();
        String expression = "INVALID: metadata.labels.env IN (production, staging)";

        // Then
        Assertions.assertThrows(SelectorException.class,
                // When
                () -> factory.make(List.of(expression))
        );
    }
}