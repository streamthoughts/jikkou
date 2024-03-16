/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.Set;

/**
 * @param <T> Type of the {@link ResourceChange}.
 */
public abstract class BaseChangeHandler<T extends ResourceChange> implements ChangeHandler<T> {

    private final Set<Operation> supportedOperations;


    /**
     * Creates a new {@link BaseChangeHandler} instance.
     *
     * @param supportedOperation The supported change type.
     */
    public BaseChangeHandler(final Operation supportedOperation) {
        this(Set.of(supportedOperation));
    }

    /**
     * Creates a new {@link BaseChangeHandler} instance.
     *
     * @param supportedOperations The supported change types.
     */
    public BaseChangeHandler(final Set<Operation> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return supportedOperations;
    }
}
