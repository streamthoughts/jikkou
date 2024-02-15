/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

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