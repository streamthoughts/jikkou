/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * Selector matching resources by combining multiple selectors.
 */
public class AggregateSelector implements Selector {

    protected final List<? extends Selector> selectors;

    private final SelectorMatchingStrategy strategy;

    /**
     * Creates a new {@link AggregateSelector} instance.
     *
     * @param selectors the list of {@link Selector}.
     */
    public AggregateSelector(List<? extends Selector> selectors,
                             SelectorMatchingStrategy strategy) {
        this.selectors = Objects.requireNonNull(selectors, "selectors cannot be null");
        this.strategy = Objects.requireNonNull(strategy, "strategy cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        final Stream<? extends Selector> stream = selectors.stream();
        return switch (strategy) {
            case NONE -> stream.noneMatch(selector -> selector.apply(resource));
            case ALL -> stream.allMatch(selector -> selector.apply(resource));
            case ANY -> stream.anyMatch(selector -> selector.apply(resource));
        };
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<String> getSelectorExpressions() {
        return selectors.stream()
                .map(Selector::getSelectorExpressions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SelectorMatchingStrategy getSelectorMatchingStrategy() {
        return strategy;
    }
}
