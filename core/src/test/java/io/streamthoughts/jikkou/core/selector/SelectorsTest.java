/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SelectorsTest {

    static final List<Selector> EXPRESSIONS = List.of(
            getSelectorForExpressionString("expr1"),
            getSelectorForExpressionString("expr2")
    );

    @Test
    void shouldGetSelectorForAllMatch() {
        Selector selector = Selectors.allMatch(EXPRESSIONS);
        Assertions.assertEquals(List.of("expr1", "expr2"), selector.getSelectorExpressions());
        Assertions.assertEquals(SelectorMatchingStrategy.ALL, selector.getSelectorMatchingStrategy());
    }

    @Test
    void shouldGetSelectorForAnyMatch() {
        Selector selector = Selectors.anyMatch(EXPRESSIONS);
        Assertions.assertEquals(List.of("expr1", "expr2"), selector.getSelectorExpressions());
        Assertions.assertEquals(SelectorMatchingStrategy.ANY, selector.getSelectorMatchingStrategy());
    }

    @Test
    void shouldGetSelectorForNoneMatch() {
        Selector selector = Selectors.noneMatch(EXPRESSIONS);
        Assertions.assertEquals(List.of("expr1", "expr2"), selector.getSelectorExpressions());
        Assertions.assertEquals(SelectorMatchingStrategy.NONE, selector.getSelectorMatchingStrategy());
    }

    @Test
    void shouldReturnFalseForNoSelector() {
        assertFalse(Selectors.containsLabelSelector(Selectors.NO_SELECTOR));
    }

    @Test
    void shouldReturnTrueForLabelSelector() {
        PreparedExpression expr = new PreparedExpression("env", ExpressionOperator.EXISTS, List.of());
        LabelSelector labelSelector = new LabelSelector(expr);
        assertTrue(Selectors.containsLabelSelector(labelSelector));
    }

    @Test
    void shouldReturnFalseForFieldSelector() {
        PreparedExpression expr = new PreparedExpression("metadata.name", ExpressionOperator.IN, List.of("test"));
        FieldSelector fieldSelector = new FieldSelector(expr);
        assertFalse(Selectors.containsLabelSelector(fieldSelector));
    }

    @Test
    void shouldReturnTrueForAggregateSelectorWithNestedLabelSelector() {
        PreparedExpression expr = new PreparedExpression("env", ExpressionOperator.EXISTS, List.of());
        LabelSelector labelSelector = new LabelSelector(expr);
        Selector aggregate = Selectors.allMatch(List.of(labelSelector));
        assertTrue(Selectors.containsLabelSelector(aggregate));
    }

    @Test
    void shouldReturnFalseForAggregateSelectorWithoutLabelSelector() {
        PreparedExpression expr = new PreparedExpression("metadata.name", ExpressionOperator.IN, List.of("test"));
        FieldSelector fieldSelector = new FieldSelector(expr);
        Selector aggregate = Selectors.allMatch(List.of(fieldSelector));
        assertFalse(Selectors.containsLabelSelector(aggregate));
    }

    @NotNull
    private static Selector getSelectorForExpressionString(String expressonString) {
        return new Selector() {
            @Override
            public boolean apply(@NotNull HasMetadata resource) {
                return false;
            }

            @Override
            public List<String> getSelectorExpressions() {
                return List.of(expressonString);
            }
        };
    }
}