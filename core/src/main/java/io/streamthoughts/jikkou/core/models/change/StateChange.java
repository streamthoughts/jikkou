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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.Nameable;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;

/**
 * The state of a data before and after a reconciliation operation.
 *
 * @see Operation
 */
@Reflectable
@JsonPropertyOrder({"name", "before", "after", "op", "description"})
@JsonDeserialize(as = GenericStateChange.class)
public interface StateChange extends Change, Nameable<StateChange> {

    /**
     * Gets the name of the changed data.
     *
     * @return The name.
     */
    @JsonProperty("name")
    String getName();

    /**
     * Gets the state of the data before the operation.
     *
     * @return The data value.
     */
    @JsonProperty("before")
    Object getBefore();

    /**
     * Gets the state of the data after the operation.
     *
     * @return The data value.
     */
    @JsonProperty("after")
    Object getAfter();

    /**
     * Gets a description of the state or change.
     *
     * @return The description.
     */
    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    default String getDescription() {
        return null;
    }

    static <T> SpecificStateChangeBuilder<T> builder() {
        return new SpecificStateChangeBuilder<>();
    }

    /**
     * Static helper method to create a new {@link StateChange} instance.
     *
     * @param name   The data change name.
     * @param before The state of the data before the operation.
     * @param after  The state of the data after the operation.
     * @return a new {@link SpecificStateChange}
     */
    static <T> SpecificStateChange<T> with(String name, T before, T after) {
        return new SpecificStateChangeBuilder<T>()
                .withName(name)
                .withBefore(before)
                .withAfter(after)
                .build();
    }

    /**
     * Static helper method to create a new {@link StateChange} instance.
     *
     * @param value The after value.
     * @return a new {@link SpecificStateChange}
     */
    static <T> SpecificStateChange<T> create(String name, T value) {
        return new SpecificStateChangeBuilder<T>()
                .withName(name)
                .withOp(CREATE)
                .withAfter(value)
                .build();
    }

    /**
     * Static helper method to create a new {@link StateChange} instance.
     *
     * @param value The before/after value.
     * @return a new {@link SpecificStateChange}
     */
    static <T> SpecificStateChange<T> none(String name, T value) {
        return new SpecificStateChangeBuilder<T>()
                .withName(name)
                .withOp(NONE)
                .withBefore(value)
                .withAfter(value)
                .build();
    }

    /**
     * Static helper method to create a new {@link StateChange} instance.
     *
     * @param before The before value.
     * @return a new {@link SpecificStateChange}
     */
    static <T> SpecificStateChange<T> delete(String name, T before) {
        return new SpecificStateChangeBuilder<T>()
                .withName(name)
                .withOp(DELETE)
                .withBefore(before)
                .build();
    }

    /**
     * Static helper method to create a new {@link StateChange} instance.
     *
     * @param before The before value.
     * @param after  The after value.
     * @return a new {@link SpecificStateChange}
     */
    static <T> SpecificStateChange<T> update(String name, T before, T after) {
        return new SpecificStateChangeBuilder<T>()
                .withName(name)
                .withOp(UPDATE)
                .withBefore(before)
                .withAfter(after)
                .build();
    }
}
