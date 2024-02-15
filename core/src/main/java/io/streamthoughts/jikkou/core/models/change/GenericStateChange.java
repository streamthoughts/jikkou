/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * The change of a named data value.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("The change of a named data value.")
@JsonPropertyOrder({
        "name",
        "op",
        "before",
        "after",
        "description"
})
@JsonDeserialize
@Reflectable
public class GenericStateChange implements StateChange {

    private final String name;
    private final Operation op;
    private final Object before;
    private final Object after;
    private final String description;

    /**
     * Creates a new {@link GenericStateChange} instance.
     *
     * @param name        The name of the data value.
     * @param op          The operation
     * @param before      The state before the operation.
     * @param after       The state after the operation.
     */
    public GenericStateChange(final String name,
                              final Operation op,
                              final Object before,
                              final Object after) {
        this(name, op, before, after, null);
    }

    /**
     * Creates a new {@link GenericStateChange} instance.
     *
     * @param name        The name of the data value.
     * @param op          The operation
     * @param before      The state before the operation.
     * @param after       The state after the operation.
     * @param description The description.
     */
    @ConstructorProperties({
            "name",
            "op",
            "before",
            "after",
            "description"
    })
    public GenericStateChange(final String name,
                              final Operation op,
                              final Object before,
                              final Object after,
                              final String description) {
        this.name = name;
        this.op = op;
        this.before = before;
        this.after = after;
        this.description = description;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public GenericStateChange withName(String name) {
        return new GenericStateChange(name, op, before, after, description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Operation getOp() {
        return op;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Object getBefore() {
        return before;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Object getAfter() {
        return after;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericStateChange that = (GenericStateChange) o;
        return Objects.equals(name, that.name) && op == that.op &&
                Objects.equals(before, that.before) &&
                Objects.equals(after, that.after) &&
                Objects.equals(description, that.description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(name, op, before, after, description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "StateChange[" +
                "name=" + name +
                ", op=" + op +
                ", before=" + before +
                ", after=" + after +
                ", description=" + description +
                ']';
    }
}
