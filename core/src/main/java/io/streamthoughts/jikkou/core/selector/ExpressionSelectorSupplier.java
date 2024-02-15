/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import org.jetbrains.annotations.NotNull;

/**
 * Supplier interface to create new {@link Selector} instance.
 */
interface ExpressionSelectorSupplier {

    /**
     * Gets a new selector instance for the specified expression
     * @param expression    the selector expressions.
     * @return  The Selector.
     */
    @NotNull Selector get(@NotNull SelectorExpression expression);
}