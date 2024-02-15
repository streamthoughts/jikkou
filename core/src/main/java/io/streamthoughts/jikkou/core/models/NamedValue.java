/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named value.
 */
public class NamedValue implements Nameable<NamedValue> {

    public final Object value;
    public final String name;

    /**
     * Creates a new {@link NamedValue} instance.
     *
     * @param name  the name attached to the value.
     * @param value the value.
     */
    public NamedValue(final String name,
                      final Object value) {
        this.name = Objects.requireNonNull(name, "name should not be null");
        this.value = Objects.requireNonNull(value, "value should not be null");
    }

    /** {@inheritDoc} **/
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} **/
    @Override
    public NamedValue withName(String name) {
        return new NamedValue(name, value);
    }

    public Object getValue() {
        return value;
    }

    public Map<String, Object> asSingletonMap() {
        return Map.of(name, value);
    }

    public Map.Entry<String, Object> asMapEntry() {
        return new AbstractMap.SimpleEntry<>(name, value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValue that = (NamedValue) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String
    toString() {
        return "[" +
                "name='" + name +
                ", value=" + value +
                ']';
    }
}
