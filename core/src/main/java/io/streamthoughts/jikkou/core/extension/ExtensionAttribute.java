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
package io.streamthoughts.jikkou.core.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an attribute of an extension. An attribute is associated with one
 * or multiple key values called members. Each member may have a default value.
 */
public final class ExtensionAttribute {

    private final String name;
    private final Map<String, Object> values;
    private final Map<String, Object> defaultValues;

    /**
     * Creates a new {@link ExtensionAttribute} instance.
     *
     * @param name the attribute name.
     */
    public ExtensionAttribute(final String name) {
        this(name, new HashMap<>(), new HashMap<>());
    }

    /**
     * Creates a new {@link ExtensionAttribute} instance.
     *
     * @param name          the attribute name.
     * @param values        the attribute values.
     * @param defaultValues the attribute default values.
     */
    public ExtensionAttribute(final String name,
                              final Map<String, Object> values,
                              final Map<String, Object> defaultValues) {
        this.name = name;
        this.values = values;
        this.defaultValues = defaultValues;
    }

    /**
     * Adds a member to this attribute with the specified member, value, and default value.
     *
     * @param member       the name of the member attribute. Cannot be {@code null}.
     * @param value        the value of the  member attribute. Cannot be {@code null}.
     * @param defaultValue the default value of the attribute. Cannot be {@code null}.
     * @throws NullPointerException – if the specified member or value is null.
     */
    public ExtensionAttribute add(@NotNull final String member,
                                  @NotNull final Object value,
                                  @Nullable final Object defaultValue) {
        values.put(member, value);
        defaultValues.put(member, defaultValue);
        return this;
    }

    /**
     * Adds an attribute for the specified name and value.
     *
     * @param member the name of the member attribute. Cannot be {@code null}.
     * @param value  the value of the  member attribute. Cannot be {@code null}.
     * @throws NullPointerException – if the specified name or value is null.
     */
    public ExtensionAttribute add(@NotNull final String member,
                                  @NotNull final Object value) {
        add(member, value, null);
        return this;
    }

    /**
     * Gets the name of this attribute.
     *
     * @return the string name.
     */
    public String name() {
        return name;
    }

    /**
     * Gets the value of the given member.
     *
     * @param member the name of the member.
     * @return the value, default value, or {@code null} if no value exist of the given member.
     */
    public Object value(final String member) {
        return Optional
                .ofNullable(values.get(member))
                .orElse(defaultValues.getOrDefault(member, null));
    }


    public boolean contains(final String member) {
        return values.containsKey(member) || defaultValues.containsKey(member);
    }

    public boolean contains(final String member, final Object value) {
        return Optional.ofNullable(value(member)).map(value::equals).orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionAttribute)) return false;
        ExtensionAttribute that = (ExtensionAttribute) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(values, that.values) &&
                Objects.equals(defaultValues, that.defaultValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, values, defaultValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[name=" + name + ", values=" + values + ", defaults=" + defaultValues + "]";
    }
}
