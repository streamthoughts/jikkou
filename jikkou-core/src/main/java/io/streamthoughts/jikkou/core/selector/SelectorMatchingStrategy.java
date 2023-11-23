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

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Strategy for matching resources when multiple selectors are used.
 */
@Reflectable
public enum SelectorMatchingStrategy {

    NONE {
        /** {@inheritDoc} **/
        @Override
        public Selector combines(final List<Selector> selectors) {
            return Selectors.noneMatch(selectors);
        }
    },

    ALL {
        /** {@inheritDoc} **/
        @Override
        public Selector combines(final List<Selector> selectors) {
            return Selectors.allMatch(selectors);
        }
    },

    ANY {
        /** {@inheritDoc} **/
        @Override
        public Selector combines(final List<Selector> selectors) {
            return Selectors.anyMatch(selectors);
        }
    };

    /**
     * Aggregates the specified list of selectors.
     *
     * @param selectors The selectors.
     * @return The new {@link Selector}.
     */
    public abstract Selector combines(final List<Selector> selectors);

    /**
     * Returns the SelectorMatchingStrategy for the specified string representation.
     *
     * @param str The string representation.
     * @return The SelectorMatchingStrategy.
     * @throws IllegalArgumentException if the specified value is not supported.
     */
    @JsonCreator
    public static SelectorMatchingStrategy fromStringIgnoreCase(final @Nullable String str) {
        if (str == null) throw new IllegalArgumentException("Unsupported SelectorMatch 'null'");
        return Arrays.stream(SelectorMatchingStrategy.values())
                .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported SelectorMatchingStrategy '" + str + "'"));
    }
}