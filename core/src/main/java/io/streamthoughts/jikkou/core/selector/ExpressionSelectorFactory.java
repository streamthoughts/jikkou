/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ExpressionSelectorFactory implements ExpressionSelectorSupplier {

    public static final String FIELD_SELECTOR = "field";
    private final ExpressionSelectorSupplier defaultSelector;
    private final Map<String, ExpressionSelectorSupplier> selectors;

    /**
     * Creates a new {@link ExpressionSelectorFactory} instance.
     */
    public ExpressionSelectorFactory() {
        this.defaultSelector = FieldSelector::new;
        this.selectors = new LinkedHashMap<>();
        this.selectors.put(FIELD_SELECTOR, defaultSelector);
    }

    /**
     * Make a {@link Selector} from the given expression string.
     *
     * @param expressionStrings the expression string.
     * @return a new {@link Selector}.
     */
    public List<Selector> make(@Nullable final String[] expressionStrings) {
        if (expressionStrings == null || expressionStrings.length == 0)
            return Collections.emptyList();

        return make(Arrays.asList(expressionStrings));
    }

    /**
     * Make a {@link Selector} from the given expression string.
     *
     * @param expressionStrings the expression string.
     * @return a new {@link Selector}.
     */
    public List<Selector> make(@Nullable final List<String> expressionStrings) {
        if (expressionStrings == null || expressionStrings.isEmpty())
            return Collections.emptyList();

        SelectorExpressionParser parser = new SelectorExpressionParser();
        return expressionStrings.stream()
                .flatMap(expressionString -> parser.parseExpressionString(expressionString).stream())
                .map(this::get)
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Selector get(@NotNull SelectorExpression expression) {
        String name = expression.selector();
        if (name == null) return defaultSelector.get(expression);

        return Optional
                .ofNullable(selectors.get(name))
                .map(supplier -> supplier.get(expression))
                .orElseThrow(() -> new SelectorException(String.format(
                        "Failed to find a selector for name '%s'. Supported are: %s.",
                        name,
                        selectors.keySet()
                )));
    }
}
