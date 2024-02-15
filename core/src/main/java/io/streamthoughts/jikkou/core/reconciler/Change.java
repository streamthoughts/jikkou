/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a change operation on a resource entity.
 */
@Evolving
@Reflectable
public interface Change {

    /**
     * The operation.
     *
     * @return The {@link Operation}.
     */
    Operation getOp();

    /**
     * Computes a common change type from all changes. This method will return {@link Operation#NONE} if all
     * given changes are of type {@link Operation#NONE}, otherwise it returns {@link Operation#UPDATE}.
     *
     * @param changes the list of changes.
     * @return a {@link Operation}.
     */
    static Operation computeOperation(Change... changes) {
        return computeOperation(Arrays.asList(changes));
    }

    /**
     * Computes a common change type from all changes.
     *
     * @param changes the list of changes.
     * @return a {@link Operation}.
     */
    static Operation computeOperation(List<? extends Change> changes) {
        if (changes == null || changes.isEmpty()) return NONE;

        Set<Operation> operations = changes
                .stream()
                .map(Change::getOp)
                .collect(Collectors.toSet());

        return operations.size() == 1 ? operations.iterator().next() : UPDATE;
    }
}
