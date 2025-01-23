/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.exceptions.InvalidSelectorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultSelectorParserTest {

    private final DefaultSelectorParser selector = new DefaultSelectorParser(FieldSelector::new);

    @Test
    void shouldParseWithMultipleValues() {
        // GIVEN
        String expressionString = "metadata.labels.env in (production, staging)";

        // WHEN
        List<Selector> results = selector.parseExpression(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        PreparedExpression result = ((FieldSelector)results.get(0)).preparedExpression();
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Arrays.asList("production", "staging"), result.values());
    }

    @Test
    void shouldParseWithSingleValue() {
        // GIVEN
        String expressionString = "metadata.labels.env in (production)";

        // WHEN
        List<Selector> results = selector.parseExpression(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        PreparedExpression result = ((FieldSelector)results.get(0)).preparedExpression();
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Collections.singletonList("production"), result.values());
    }

    @Test
    void shouldParseWithNoValue() {
        // GIVEN
        String expressionString = "metadata.labels.env exists";

        // WHEN
        List<Selector> results = selector.parseExpression(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        PreparedExpression result = ((FieldSelector)results.get(0)).preparedExpression();
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.EXISTS, result.operator());
        Assertions.assertTrue(result.values().isEmpty());
    }

    @Test
    void shouldParseWithMultipleConditions() {
        // GIVEN
        String expressionString = "metadata.labels.env in (production, staging), metadata.labels.dummy exists";

        // WHEN
        List<Selector> results = selector.parseExpression(expressionString);

        // THEN
        Assertions.assertEquals(2, results.size());

        PreparedExpression result1 = ((FieldSelector)results.get(0)).preparedExpression();
        Assertions.assertEquals("metadata.labels.env", result1.key());
        Assertions.assertEquals(ExpressionOperator.IN, result1.operator());
        Assertions.assertIterableEquals(Arrays.asList("production", "staging"), result1.values());

        PreparedExpression result2 = ((FieldSelector)results.get(1)).preparedExpression();
        Assertions.assertEquals("metadata.labels.dummy", result2.key());
        Assertions.assertEquals(ExpressionOperator.EXISTS, result2.operator());
        Assertions.assertTrue(result2.values().isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidSelectorString() {
        // GIVEN
        String expressionString = "invalid_selector_string";

        // WHEN and THEN
        Assertions.assertThrows(
                InvalidSelectorException.class,
                () ->  {
                    selector.parseExpression(expressionString);
                }
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForBlankSelectorString() {
        // GIVEN
        String expressionString = " ";

        // WHEN and THEN
        Assertions.assertThrows(
                InvalidSelectorException.class,
                () ->  {
                    selector.parseExpression(expressionString);
                }
        );
    }
}