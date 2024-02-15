/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Determines the type of changes that can be applied on the resources to be reconciled.
 */
public enum ReconciliationMode {

    /**
     * Only changes that create new resource objects on the system will be applied.
     */
    CREATE(Operation.CREATE),

    /**
     * Only changes that delete existing resource objects on your system will be applied.
     */
    DELETE(Operation.DELETE),

    /**
     * Only changes that create or update existing resource objects on the system will be applied.
     */
    UPDATE(Operation.CREATE, Operation.UPDATE),

    /**
     * Apply all reconciliation changes
     */
    FULL(Operation.CREATE, Operation.UPDATE, Operation.DELETE);

    @JsonCreator
    public static ReconciliationMode getForNameIgnoreCase(final @Nullable String str) {
        return Enums.getForNameIgnoreCase(str, ReconciliationMode.class);
    }

    /**
     * Set of change-type supported for this reconciliation mode.
     */
    private final Set<Operation> operations;

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param operations {@link #operations}
     */
    ReconciliationMode(Operation... operations) {
        this(Set.of(operations));
    }

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param operations {@link #operations}
     */
    ReconciliationMode(Set<Operation> operations) {
        this.operations = operations;
    }

    /**
     * Checks whether the given change is supported by this reconciliation mode.
     *
     * @param change the change to test.
     * @return {@code true} if the change is supported, otherwise {@code false}.
     */
    public boolean isSupported(ResourceChange change) {
        Operation operation = change.getSpec().getOp();
        return operation == Operation.NONE || operations.contains(operation);
    }
}
