/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.exceptions.InvalidSelectorException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SelectorExpression.
 *
 * @param expression the expression string.
 * @param selector   the name of the selector.
 * @param key        the expression key.
 * @param operator   the expression operator.
 * @param values     the expression values.
 */
public record SelectorExpression(@NotNull String expression,
                                 @Nullable String selector,
                                 @NotNull String key,
                                 @NotNull ExpressionOperator operator,
                                 @NotNull List<String> values) {

    /**
     * Creates a new {@link SelectorExpression} instance.
     */
    public SelectorExpression {
        if (operator == ExpressionOperator.INVALID) {
            throw new InvalidSelectorException(
                    "Unknown operator: '"
                            + operator + "' in expression '"
                            + expression + "' Valid are: " + ExpressionOperator.validSet()
            );
        }
    }

    /**
     * Creates a new {@link SelectorExpression} instance.
     *
     * @param expression the expression string.
     * @param selector   the name of the selector.
     * @param key        the expression key.
     * @param operator   the expression operator.
     * @param values     the expression values.
     */
    public SelectorExpression(@NotNull String expression,
                              @Nullable String selector,
                              @NotNull String key,
                              @NotNull String operator,
                              @NotNull List<String> values) {
        this(expression, selector, key, ExpressionOperator.findByName(operator), values);
    }

    public MatchExpression create(ExpressionKeyValueExtractor extractor) {
        return operator().create(key, values, extractor);
    }
}
