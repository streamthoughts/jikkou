/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Objects;

/**
 *
 */
public class ConfigValue {

    private final String name;

    private final Object value;

    private final boolean isDefault;

    private final boolean isDeletable;

    /**
     * Creates a new {@link ConfigValue} instance.
     *
     * @param name  the property name.
     * @param value the property value.
     */
    public ConfigValue(final String name,
                       final Object value) {
        this(name, value, false, true);
    }

    /**
     * Creates a new {@link ConfigValue} instance.
     *
     * @param name      the property name.
     * @param value     the property value.
     * @param isDefault is this value default.
     */
    public ConfigValue(final String name,
                       final Object value,
                       final boolean isDefault,
                       final boolean isDeletable) {
        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
        this.isDeletable = isDeletable;
    }

    public String getName() {
        return name;
    }

    public Object value() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigValue)) return false;
        ConfigValue that = (ConfigValue) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(String.valueOf(value), String.valueOf(that.value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "ConfigValue{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", isDefault=" + isDefault +
                ", isDeletable=" + isDeletable +
                '}';
    }
}
