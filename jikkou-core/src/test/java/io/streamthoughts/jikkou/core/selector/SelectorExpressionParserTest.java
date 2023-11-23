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

import io.streamthoughts.jikkou.core.exceptions.InvalidSelectorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SelectorExpressionParserTest {

    @Test
    void shouldParseSelectorWithNoSelector() {
        // GIVEN
        String expressionString = "metadata.labels.env in (production, staging)";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        SelectorExpression result = results.get(0);
        Assertions.assertNull(result.selector());
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Arrays.asList("production", "staging"), result.values());
    }

    @Test
    void shouldParseSelectorWithMultipleValues() {
        // GIVEN
        String expressionString = "selector: metadata.labels.env in (production, staging)";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        SelectorExpression result = results.get(0);
        Assertions.assertEquals("selector", result.selector());
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Arrays.asList("production", "staging"), result.values());
    }

    @Test
    void shouldParseSelectorWithSingleValue() {
        // GIVEN
        String expressionString = "selector: metadata.labels.env in (production)";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        SelectorExpression result = results.get(0);
        Assertions.assertEquals("selector", result.selector());
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Collections.singletonList("production"), result.values());
    }

    @Test
    void shouldParseSelectorWithNoValue() {
        // GIVEN
        String expressionString = "selector: metadata.labels.env exists";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        SelectorExpression result = results.get(0);
        Assertions.assertEquals("selector", result.selector());
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.EXISTS, result.operator());
        Assertions.assertTrue(result.values().isEmpty());
    }

    @Test
    void shouldParseSelectorWithExtraSpaces() {
        // GIVEN
        String expressionString = "selector:     metadata.labels.env in production";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(1, results.size());

        SelectorExpression result = results.get(0);
        Assertions.assertEquals("selector", result.selector());
        Assertions.assertEquals("metadata.labels.env", result.key());
        Assertions.assertEquals(ExpressionOperator.IN, result.operator());
        Assertions.assertIterableEquals(Collections.singletonList("production"), result.values());
    }

    @Test
    void shouldParseSelectorWithMultipleConditions() {
        // GIVEN
        String expressionString = "selector: metadata.labels.env in (production, staging), metadata.labels.dummy exists";

        // WHEN
        SelectorExpressionParser selector = new SelectorExpressionParser();
        List<SelectorExpression> results = selector.parseExpressionString(expressionString);

        // THEN
        Assertions.assertEquals(2, results.size());

        SelectorExpression result1 = results.get(0);
        Assertions.assertEquals("selector", result1.selector());
        Assertions.assertEquals("metadata.labels.env", result1.key());
        Assertions.assertEquals(ExpressionOperator.IN, result1.operator());
        Assertions.assertIterableEquals(Arrays.asList("production", "staging"), result1.values());

        SelectorExpression result2 = results.get(1);
        Assertions.assertEquals("selector", result2.selector());
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
                    SelectorExpressionParser selector = new SelectorExpressionParser();
                    selector.parseExpressionString(expressionString);
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
                    SelectorExpressionParser selector = new SelectorExpressionParser();
                    selector.parseExpressionString(expressionString);
                }
        );
    }
}