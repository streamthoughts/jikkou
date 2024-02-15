/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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