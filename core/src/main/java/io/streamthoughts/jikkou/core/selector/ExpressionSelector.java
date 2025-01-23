/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.policy.CelExpression;
import io.streamthoughts.jikkou.core.policy.CelExpressionFactory;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A selector implementation based on Common Expression Language (Cel).
 */
public class ExpressionSelector implements Selector {

    private final CelExpression<Boolean> expression;

    /**
     * Creates a new {@link ExpressionSelector} instance.
     *
     * @param expression    The string expression.
     */
    public ExpressionSelector(final String expression) {
        Objects.requireNonNull(expression, "expression cannot be null");
        this.expression = CelExpressionFactory.bool().compile(expression);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return expression.eval(resource);
    }
}
