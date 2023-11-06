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
