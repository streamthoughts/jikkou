/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import java.util.List;

class AllMatchSelector extends AggregateSelector {

    /**
     * Creates a new {@link AllMatchSelector} instance.
     *
     * @param selectors The list of {@link Selector}.
     */
    public AllMatchSelector(List<? extends Selector> selectors) {
        super(selectors, SelectorMatchingStrategy.ALL);
    }
}
