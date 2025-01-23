/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.List;

/**
 * Default interface for selecting resources that should be involved in the reconciliation process.
 */
@Evolving
public interface Selector extends MatchExpression {


    /**
     * Gets the strategy used by this selector for matching resources.
     * @return The SelectorMatchingStrategy.
     */
    default SelectorMatchingStrategy getSelectorMatchingStrategy() {
        return SelectorMatchingStrategy.ALL;
    }

    /**
     * Gets the string selector expressions for this selector.
     *
     * @return  The list of selector string expressions.
     */
    default List<String> getSelectorExpressions() {
        throw new UnsupportedOperationException();
    }

}
