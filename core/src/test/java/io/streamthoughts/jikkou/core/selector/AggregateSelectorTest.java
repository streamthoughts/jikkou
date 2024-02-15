/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AggregateSelectorTest {

    @Test
    void shouldMatchForAllStrategy() {
        // GIVEN
        List<Selector> selectors = List.of(
                resource -> resource.getMetadata().getName().startsWith("A"), // TRUE
                resource -> resource.getMetadata().getName().endsWith("C") // TRUE
        );
        Selector selector = new AggregateSelector(selectors, SelectorMatchingStrategy.ALL);

        // WHEN THEN
        Assertions.assertTrue(selector.apply(getHasMetadataForName("ABC")));
        Assertions.assertFalse(selector.apply(getHasMetadataForName("CBA")));
    }

    @Test
    void shouldMatchForAnyStrategy() {
        // GIVEN
        List<Selector> selectors = List.of(
                resource -> resource.getMetadata().getName().startsWith("B"), // FALSE
                resource -> resource.getMetadata().getName().endsWith("C") // TRUE
        );
        Selector selector = new AggregateSelector(selectors, SelectorMatchingStrategy.ANY);
        // WHEN THEN
        Assertions.assertTrue(selector.apply(getHasMetadataForName("BCD")));
        Assertions.assertTrue(selector.apply(getHasMetadataForName("ABC")));
        Assertions.assertFalse(selector.apply(getHasMetadataForName("AAA")));
    }

    @Test
    void shouldMatchForNoneStrategy() {
        // GIVEN
        List<Selector> selectors = List.of(
                resource -> resource.getMetadata().getName().equals("ABC") // TRUE
        );
        Selector selector = new AggregateSelector(selectors, SelectorMatchingStrategy.NONE);
        // WHEN THEN
        Assertions.assertFalse(selector.apply(getHasMetadataForName("ABC")));
        Assertions.assertTrue(selector.apply(getHasMetadataForName("DEF")));
    }

    @NotNull
    private HasMetadata getHasMetadataForName(final String name) {
        return new HasMetadata() {
            @Override
            public ObjectMeta getMetadata() {
                return new ObjectMeta(name);
            }

            @Override
            public HasMetadata withMetadata(ObjectMeta metadata) {
                return this;
            }
        };
    }


}