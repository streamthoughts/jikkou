/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.change;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueChange<T> {

    private final T after;
    private final T before;
    private final Change.OperationType op;

    /**
     * Static helper method to create a new {@link ValueChange<T>} instance.
     *
     * @param value   the target value.
     * @param <T>     the value type.
     * @return        a new {@link ValueChange<T>}
     */
    public static <T> ValueChange<T> withAfterValue(@Nullable final T value) {
        return with(value, null);
    }

    /**
     * Static helper method to create a new {@link ValueChange<T>} instance.
     *
     * @param value    the previous value.
     * @param <T>      the value type.
     * @return         a new {@link ValueChange<T>}
     */
    public static <T> ValueChange<T> withBeforeValue(@Nullable final T value) {
        return with(null, value);
    }

    /**
     * Static helper method to create a new {@link ValueChange<T>} instance.
     *
     * @param after   the target value.
     * @param before  the previous value.
     * @param <T>           the value type.
     * @return              a new {@link ValueChange<T>}
     */
    public static <T> ValueChange<T> with(@Nullable final T after,
                                          @Nullable final T before) {

        if (after == null && before == null) {
            throw new IllegalArgumentException("Target and previous value cannot be both 'null'.");
        }
        if (after != null && before != null) {
            return after.equals(before) ?
                    new ValueChange<>(after, before, Change.OperationType.NONE) :
                    new ValueChange<>(after, before, Change.OperationType.UPDATE);

        }

        if (after == null) {
            return new ValueChange<>(null, before, Change.OperationType.DELETE);
        }

        // before is NULL
        return new ValueChange<>(after, null, Change.OperationType.ADD);
    }

    /**
     * Creates a new {@link ValueChange} instance.
     *
     * @param after      the target value.
     * @param before     the previous value.
     * @param op         the operation type.
     */
    private ValueChange(@Nullable final T after,
                        @Nullable final T before,
                        @NotNull final Change.OperationType op) {
        this.after = after;
        this.before = before;
        this.op = op;
    }

    /**
     * Creates a new {@link ValueChange} instance.
     * @param change    the {@link ValueChange} to copy.
     */
    protected ValueChange(@NotNull final ValueChange<T> change) {
        this.after = change.after;
        this.before = change.before;
        this.op = change.op;
    }

    public T getAfter() {
        return after;
    }

    public T getBefore() {
        return before;
    }

    public Change.OperationType getOperation() {
        return op;
    }
}
