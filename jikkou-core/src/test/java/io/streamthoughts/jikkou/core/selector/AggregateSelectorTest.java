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
            public HasMetadata withMetadata(ObjectMeta objectMeta) {
                return this;
            }
        };
    }


}