/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.StateComparator;
import java.util.Objects;

/**
 * Class for building new {@link SpecificStateChange} instances.
 *
 * @param <T> Type of the state.
 */
public final class SpecificStateChangeBuilder<T> implements StateChange {

    private String name;
    private Operation op;
    private T before;
    private T after;
    private String description;
    private StateComparator<T> comparator = StateComparator.Equals();

    /** {@inheritDoc} **/
    @Override
    public SpecificStateChangeBuilder<T> withName(final String name) {
        this.name = name;
        return this;
    }

    public SpecificStateChangeBuilder<T> withComparator(final StateComparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    public SpecificStateChangeBuilder<T> withOp(final Operation op) {
        this.op = op;
        return this;
    }

    public SpecificStateChangeBuilder<T> withDescription(final String description) {
        this.description = description;
        return this;
    }

    public SpecificStateChangeBuilder<T> withAfter(final T after) {
        this.after = after;
        return this;
    }

    public SpecificStateChangeBuilder<T> withBefore(final T before) {
        this.before = before;
        return this;
    }

    /** {@inheritDoc} **/
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} **/
    @Override
    public T getBefore() {
        return before;
    }

    /** {@inheritDoc} **/
    @Override
    public T getAfter() {
        return after;
    }

    /** {@inheritDoc} **/
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} **/
    @Override
    public Operation getOp() {
        if (op != null) return  op;

        if (after == null && before == null) {
            return NONE;
        }
        if (after != null && before != null) {
            return comparator.equals(before, after) ? NONE : UPDATE;
        }

        return after == null ? DELETE : CREATE;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecificStateChangeBuilder<?> that = (SpecificStateChangeBuilder<?>) o;
        return Objects.equals(name, that.name) && op == that.op &&
                Objects.equals(before, that.before) &&
                Objects.equals(after, that.after) &&
                Objects.equals(description, that.description);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(name, op, before, after, description);
    }

    /**
     * Creates the state change.
     *
     * @return  The {@link SpecificStateChange}
     */
    public SpecificStateChange<T> build() {
        return new SpecificStateChange<>(getName(), getOp(), getBefore(), getAfter(), getDescription());
    }
}
