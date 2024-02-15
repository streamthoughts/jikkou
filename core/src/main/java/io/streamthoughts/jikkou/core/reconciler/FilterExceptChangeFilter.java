/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

final class FilterExceptChangeFilter<T extends Change> implements ChangeFilter<T> {

    private final Set<Operation> operationsToInclude;

    /**
     * Creates a new {@link FilterExceptChangeFilter} instance.
     *
     * @param operationsToInclude The operations to include.
     */
    FilterExceptChangeFilter(Set<Operation> operationsToInclude) {
        this.operationsToInclude = Optional.ofNullable(operationsToInclude).orElse(Collections.emptySet());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean test(T change) {
        return operationsToInclude.isEmpty() || operationsToInclude.contains(change.getOp());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "FilterExceptChangeFilter[" +
                ", operationsToInclude=" + operationsToInclude +
                ']';
    }
}
