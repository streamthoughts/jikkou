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
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public final class SelectorFactory{

    private final DefaultSelectorParser defaultSelectorParser;
    private final Map<String, SelectorParser> parsers;

    /**
     * Creates a new {@link SelectorFactory} instance.
     */
    public SelectorFactory() {
        this.defaultSelectorParser = new DefaultSelectorParser(FieldSelector::new);
        this.parsers = new LinkedHashMap<>();
        this.parsers.put("field", new DefaultSelectorParser(FieldSelector::new));
        this.parsers.put("label", new DefaultSelectorParser(LabelSelector::new));
        this.parsers.put("expr", expression -> List.of(new ExpressionSelector(expression)));
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

        return expressionStrings.stream()
                .flatMap(this::parseOrThrow)
                .toList();
    }

    private Stream<Selector> parseOrThrow(final String expressionString) {
        // Parse the selector
        String[] parts = expressionString.split(":", 2);

        String selector = null;
        String conditionsString = expressionString;
        if (parts.length == 2) {
            selector = parts[0].trim();
            conditionsString = parts[1].trim();
        }

        if (selector == null) {
            return defaultSelectorParser.parseExpression(expressionString).stream();
        } else {
            final SelectorParser parser = parsers.get(selector);
            if (parser != null) {
                return parser.parseExpression(conditionsString).stream();
            }
            throw new SelectorException("Failed to find a selector for name '%s'. Supported are: %s."
                .formatted(selector, parsers.keySet())
            );
        }
    }
}
