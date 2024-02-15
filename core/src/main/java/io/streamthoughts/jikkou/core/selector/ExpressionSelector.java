/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ExpressionSelector implements Selector {

    private final ExpressionKeyValueExtractor keyExtractor;
    private final SelectorExpression expression;

    /**
     * Creates a new {@link ExpressionSelector} instance.
     *
     * @param expression The SelectorExpression.
     * @param keyExtractor The ExpressionKeyValueExtractor.
     */
    public ExpressionSelector(final @NotNull SelectorExpression expression,
                              final @NotNull ExpressionKeyValueExtractor keyExtractor) {
        this.expression = Objects.requireNonNull(
                expression, "'expression' must not be null");
        this.keyExtractor = Objects.requireNonNull(
                keyExtractor, "'keyResourceExtractor' must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return expression.create(keyExtractor).apply(resource);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<String> getSelectorExpressions() {
        return List.of(expression.expression());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return expression.toString();
    }
}
