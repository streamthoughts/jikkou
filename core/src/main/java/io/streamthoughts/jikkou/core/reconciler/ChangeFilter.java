/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Interface for filtering changes.
 */
public interface ChangeFilter<C extends Change> extends Predicate<C> {

    /**
     * Factory method to construct filter that filters out all changes except ones for operations includes in the given set.
     *
     * @param operations The operations.
     * @return The new {@link ChangeFilter}.
     */
    static <C extends Change> ChangeFilter<C> filterOutAllExcept(Set<Operation> operations) {
        return new FilterExceptChangeFilter<>(operations);
    }

    /**
     * Evaluates this filter on the given change.
     *
     * @param change The change.
     * @return {@code true} if the input change matches the filter, otherwise {@code false}.
     */
    @Override
    boolean test(C change);
}
