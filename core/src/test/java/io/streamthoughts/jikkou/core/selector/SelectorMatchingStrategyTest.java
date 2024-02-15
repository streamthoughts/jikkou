/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SelectorMatchingStrategyTest {

    @Test
    void shouldReturnAnyMatch() {
        Assertions.assertEquals(
                AnyMatchSelector.class,
                SelectorMatchingStrategy.ANY.combines(List.of(Selectors.NO_SELECTOR)).getClass()
        );
    }

    @Test
    void shouldReturnAllMatch() {
        Assertions.assertEquals(
                AllMatchSelector.class,
                SelectorMatchingStrategy.ALL.combines(List.of(Selectors.NO_SELECTOR)).getClass()
        );
    }

    @Test
    void shouldReturnNoneMatch() {
        Assertions.assertEquals(
                NoneMatchSelector.class,
                SelectorMatchingStrategy.NONE.combines(List.of(Selectors.NO_SELECTOR)).getClass()
        );
    }

    @Test
    void shouldGetSelectorMatchFromALLString() {
        Assertions.assertEquals(SelectorMatchingStrategy.ALL, SelectorMatchingStrategy.fromStringIgnoreCase("all"));
    }

    @Test
    void shouldGetSelectorMatchFromNONEString() {
        Assertions.assertEquals(SelectorMatchingStrategy.NONE, SelectorMatchingStrategy.fromStringIgnoreCase("none"));
    }

    @Test
    void shouldGetSelectorMatchFromANYString() {
        Assertions.assertEquals(SelectorMatchingStrategy.ANY, SelectorMatchingStrategy.fromStringIgnoreCase("any"));
    }

    @Test
    void shouldThrowExceptionForInvalidStringEnum() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SelectorMatchingStrategy.fromStringIgnoreCase("INVALID"));
    }
}