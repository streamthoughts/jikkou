/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.resources;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

/**
 *
 */
public class ConfigValue implements Named {

    private final String name;

    private final Object value;

    private final boolean isDefault;

    /**
     * Creates a new {@link ConfigValue} instance.
     *
     * @param name      the property name.
     * @param value     the property value.
     */
    public ConfigValue(final String name,
                       final Object value) {
        this(name, value, false);
    }

    /**
     * Creates a new {@link ConfigValue} instance.
     *
     * @param name      the property name.
     * @param value     the property value.
     */
    public ConfigValue(final String name,
                       final Object value,
                       final boolean isDefault) {
        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigValue)) return false;
        ConfigValue that = (ConfigValue) o;
        return isDefault == that.isDefault &&
                Objects.equals(name, that.name) &&
                Objects.equals(value.toString(), that.value.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, value, isDefault);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ConfigValue{" +
                "name=" + name +
                ", value=" + value +
                ", isDefault=" + isDefault +
                '}';
    }
}
