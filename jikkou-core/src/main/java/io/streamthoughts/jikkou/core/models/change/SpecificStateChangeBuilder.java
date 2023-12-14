/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.models.change;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

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

    public SpecificStateChangeBuilder<T> withName(final String name) {
        this.name = name;
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
            return isEquals(before, after) ? NONE : UPDATE;
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

    private static <T> boolean isEquals(@NotNull final T after,
                                        @NotNull final T before) {
        if (after instanceof String)
            return after.equals(before.toString());

        if (before instanceof String)
            return before.equals(after.toString());
        return after.equals(before);
    }
}
