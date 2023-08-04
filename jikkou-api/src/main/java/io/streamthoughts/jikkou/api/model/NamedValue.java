/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.model;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NamedValue implements Nameable {

    /**
     * Create an empty set of named values.
     *
     * @return the named value set; never null
     */
    public static Set emptySet() {
        return new Set();
    }

    /**
     * Create a set of named values.
     *
     * @param values the named values to include
     * @return the named value set; never null
     */
    public static Set setOf(Map<String, Object> values) {
        return setOf(values.entrySet().stream().map(e -> new NamedValue(e.getKey(), e.getValue())).toList());
    }

    /**
     * Create a set of named values.
     *
     * @param values the named values to include
     * @return the named value set; never null
     */
    public static Set setOf(NamedValue... values) {
        return new Set().with(values);
    }

    /**
     * Create a set of named values.
     *
     * @param values the values to include
     * @return the named value set; never null
     */
    public static Set setOf(Iterable<NamedValue> values) {
        return new Set().with(values);
    }

    /**
     * A set of NamedValue
     */
    public static class Set implements Iterable<NamedValue> {

        private final Map<String, NamedValue> valuesByName;

        private Set() {
            this.valuesByName = Collections.emptyMap();
        }

        private Set(final Collection<NamedValue> values) {
            Map<String, NamedValue> all = new LinkedHashMap<>();
            values.forEach(field -> {
                if (field != null) {
                    all.put(field.getName(), field);
                }
            });
            this.valuesByName = Collections.unmodifiableMap(all);
        }

        public Set with(NamedValue... values) {
            if (values.length == 0) {
                return this;
            }
            LinkedHashSet<NamedValue> all = new LinkedHashSet<>(this.valuesByName.values());
            for (NamedValue f : values) {
                if (f != null) {
                    all.add(f);
                }
            }
            return new Set(all);
        }

        public Set with(final Iterable<NamedValue> fields) {
            LinkedHashSet<NamedValue> all = new LinkedHashSet<>(this.valuesByName.values());
            fields.forEach(field -> {
                if (field != null) {
                    all.add(field);
                }
            });
            return new Set(all);
        }

        @Override
        public Iterator<NamedValue> iterator() {
            return valuesByName.values().iterator();
        }

        public Map<String, Object> asMap() {
            return valuesByName.values()
                    .stream()
                    .collect(Collectors.toMap(NamedValue::getName, NamedValue::getValue));
        }
    }

    public final String name;
    private final Object value;

    /**
     * Creates a new {@link NamedValue} instance.
     * @param name      the name attached to the value.
     * @param value     the value.
     */
    public NamedValue(final String name,
                      final Object value) {
        this.name = Objects.requireNonNull(name, "name should not be null");
        this.value = Objects.requireNonNull(value, "value should not be null");
    }

    /** {@inheritDoc} **/
    public String getName() {
        return name;
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

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValue that = (NamedValue) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /** {@inheritDoc} **/
    @Override
    public String
    toString() {
        return "[" +
                "name='" + name +
                ", value=" + value +
                ']';
    }
}
