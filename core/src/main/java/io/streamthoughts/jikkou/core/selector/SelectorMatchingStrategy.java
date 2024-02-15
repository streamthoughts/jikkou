/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

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