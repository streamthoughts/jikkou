/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.exceptions.InvalidSelectorException;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PreparedExpression.
 *
 * @param expression the expression string.
 * @param key        the expression key.
 * @param operator   the expression operator.
 * @param values     the expression values.
 */
public record PreparedExpression(@Nullable String expression,
                                 @NotNull String key,
                                 @NotNull ExpressionOperator operator,
                                 @NotNull List<String> values) {

    public PreparedExpression(@NotNull String key,
                              @NotNull ExpressionOperator operator,
                              @NotNull List<String> values) {
        this(null, key, operator, values);
    }

    /**
     * Creates a new {@link PreparedExpression} instance.
     */
    public PreparedExpression {
        if (operator == ExpressionOperator.INVALID) {
            throw new InvalidSelectorException(
                    "Unknown operator: '"
                            + operator + "' in expression '"
                            + expression + "' Valid are: " + ExpressionOperator.validSet()
            );
        }
    }

    /**
     * Creates a new {@link PreparedExpression} instance.
     *
     * @param expression the expression string.
     * @param key        the expression key.
     * @param operator   the expression operator.
     * @param values     the expression values.
     */
    public PreparedExpression(@NotNull String expression,
                              @NotNull String key,
                              @NotNull String operator,
                              @NotNull List<String> values) {
        this(expression, key, ExpressionOperator.findByName(operator), values);
    }

    /** {@inheritDoc} **/
    @Override
    public String expression() {
        return Optional.ofNullable(expression).orElseGet(() -> {
            if (!values.isEmpty()) {
                return key + " " + operator.name() + values;
            } else {
                return key + " " + operator.name();
            }
        });
    }

    public MatchExpression create(ExpressionKeyValueExtractor extractor) {
        return operator().create(key, values, extractor);
    }
}
