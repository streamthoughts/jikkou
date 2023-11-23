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