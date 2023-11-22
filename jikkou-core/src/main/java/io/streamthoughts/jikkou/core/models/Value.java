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

import io.streamthoughts.jikkou.core.config.internals.TypeConverter;
import java.util.Objects;

/**
 * Wrap an object value.
 */
public class Value {

    private final Object value;

    /**
     * Creates a new {@link Value} instance.
     *
     * @param value The object value. Cannot be {@code null}.
     * @throws NullPointerException if the specified value is {@code null}.
     */
    public Value(Object value) {
        this.value = Objects.requireNonNull(value, "value should not be null");
    }

    /**
     * Gets the object value
     *
     * @return The value.
     */
    public Object get() {
        return value;
    }
    /**
     * @return a short representation of the object.
     */
    public short asShort() {
        return TypeConverter.getShort(value);
    }

    /**
     * @return an int representation of the object.
     */
    public int asInt() {
        return TypeConverter.getInt(value);
    }

    /**
     * @return a long representation of the object.
     */
    public long asLong() {
        return TypeConverter.getLong(value);
    }

    /**
     * @return a float representation of the object.
     */
    public float asFloat() {
        return TypeConverter.getFloat(value);
    }

    /**
     * @return a double representation of the object.
     */
    public double asDouble() {
        return TypeConverter.getDouble(value);
    }

    /**
     * @return a boolean representation of the object.
     */
    public boolean asBoolean() {
        return TypeConverter.getBool(value);
    }

    /**
     * @return a boolean representation of the object.
     */
    public String asString() {
        return TypeConverter.getString(value);
    }

    /**
     * @return a boolean representation of the object.
     */
    public byte[] asBytes() {
        return TypeConverter.getBytes(value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value that = (Value) o;
        return Objects.equals(this.value, that.value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return value.toString();
    }
}
