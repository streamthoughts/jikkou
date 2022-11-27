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
package io.streamthoughts.jikkou.api.control;

import io.vavr.control.Option;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueChange<T> {

    private final T after;
    private final T before;
    private final ChangeType type;

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param value   the target value.
     * @param <T>     the value type.
     * @return        a new {@link ValueChange}
     */
    public static <T> ValueChange<T> withAfterValue(@Nullable final T value) {
        return with(value, null);
    }

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param value    the previous value.
     * @param <T>      the value type.
     * @return         a new {@link ValueChange}
     */
    public static <T> ValueChange<T> withBeforeValue(@Nullable final T value) {
        return with(null, value);
    }

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param after   the target value.
     * @param before  the previous value.
     * @param <T>     the value type.
     * @return        a new {@link ValueChange}
     */
    public static <T> ValueChange<T> with(@Nullable final T after,
                                          @Nullable final T before) {

        if (after == null && before == null) {
            throw new IllegalArgumentException("Target and previous value cannot be both 'null'.");
        }
        if (after != null && before != null) {
            return isEquals(after, before) ?
                    new ValueChange<>(after, before, ChangeType.NONE) :
                    new ValueChange<>(after, before, ChangeType.UPDATE);

        }

        if (after == null) {
            return new ValueChange<>(null, before, ChangeType.DELETE);
        }

        // before is NULL
        return new ValueChange<>(after, null, ChangeType.ADD);
    }

    private static <T> boolean isEquals(@NotNull final T after, @NotNull final T before) {
        if (after instanceof String)
            return after.equals(before.toString());

        if (before instanceof String)
            return before.equals(after.toString());

        return after.equals(before);
    }

    /**
     * Creates a new {@link ValueChange} instance.
     *
     * @param after      the target value.
     * @param before     the previous value.
     * @param type         the change type.
     */
    private ValueChange(@Nullable final T after,
                        @Nullable final T before,
                        @NotNull final ChangeType type) {
        this.after = after;
        this.before = before;
        this.type = type;
    }

    /**
     * Creates a new {@link ValueChange} instance.
     *
     * @param change    the {@link ValueChange} to copy.
     */
    protected ValueChange(@NotNull final ValueChange<T> change) {
        this.after = change.after;
        this.before = change.before;
        this.type = change.type;
    }

    public T getAfter() {
        return after;
    }

    public T getBefore() {
        return before;
    }

    public Option<T> tOption() {
        return type == ChangeType.NONE ? Option.none() : Option.of(after);
    }

    public ChangeType type() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueChange<?> that = (ValueChange<?>) o;
        return Objects.equals(after, that.after) && Objects.equals(before, that.before) && type == that.type;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(after, before, type);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "{" +
                "after=" + after +
                ", before=" + before +
                ", type=" + type +
                '}';
    }
}
