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

/**
 * Selectors.
 */
public final class Selectors {

    public static final Selector NO_SELECTOR = new Selector() {
        /** {@inheritDoc} **/
        @Override
        public boolean apply(@NotNull HasMetadata resource) {
            return true;
        }
    };

    /**
     * Returns an aggregated selector to only select resources that match all the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector allMatch(@NotNull List<Selector> selectors) {
        return new AllMatchSelector(selectors);
    }

    /**
     * Returns an aggregated selector to select resources that many any of the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector anyMatch(@NotNull List<Selector> selectors) {
        return new AnyMatchSelector(selectors);
    }

    /**
     * Returns an aggregated selector to only select resources that match none of the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector noneMatch(@NotNull List<Selector> selectors) {
        return new NoneMatchSelector(selectors);
    }

    /**
     * Checks whether the given selector contains a {@link LabelSelector},
     * either directly or nested within an {@link AggregateSelector}.
     *
     * @param selector the selector to check.
     * @return {@code true} if the selector contains a {@link LabelSelector}.
     */
    public static boolean containsLabelSelector(@NotNull Selector selector) {
        if (selector instanceof LabelSelector) return true;
        if (selector instanceof AggregateSelector agg) {
            return agg.selectors.stream().anyMatch(Selectors::containsLabelSelector);
        }
        return false;
    }
}
