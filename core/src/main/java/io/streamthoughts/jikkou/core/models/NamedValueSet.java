/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A set of NamedValue
 */
public class NamedValueSet implements Iterable<NamedValue> {

    private final Map<String, NamedValue> valuesByName;

    NamedValueSet() {
        this.valuesByName = Collections.emptyMap();
    }

    private NamedValueSet(final Collection<NamedValue> values) {
        this(values, false);
    }

    private NamedValueSet(final Collection<NamedValue> values, final boolean deepMerge) {
        Map<String, NamedValue> all = new LinkedHashMap<>();
        values.forEach(field -> {
            if (field != null) {
                if (deepMerge && all.containsKey(field.getName())) {
                    NamedValue existing = all.get(field.getName());
                    Object mergedValue = CollectionUtils.deepMergeValues(existing.getValue(), field.getValue());
                    all.put(field.getName(), new NamedValue(field.getName(), mergedValue));
                } else {
                    all.put(field.getName(), field);
                }
            }
        });
        this.valuesByName = Collections.unmodifiableMap(all);
    }

    /**
     * Create an empty set of named values.
     *
     * @return the named value set; never null
     */
    public static NamedValueSet emptySet() {
        return new NamedValueSet();
    }

    /**
     * Create a set of named values.
     *
     * @param values the named values to include
     * @return the named value set; never null
     */
    public static NamedValueSet setOf(Map<String, ?> values) {
        return setOf(values.entrySet().stream().map(e -> new NamedValue(e.getKey(), e.getValue())).toList());
    }

    /**
     * Create a set of named values.
     *
     * @param values the named values to include
     * @return the named value set; never null
     */
    public static NamedValueSet setOf(NamedValue... values) {
        return new NamedValueSet().with(values);
    }

    /**
     * Create a set of named values.
     *
     * @param values the values to include
     * @return the named value set; never null
     */
    public static NamedValueSet setOf(Iterable<NamedValue> values) {
        return new NamedValueSet().with(values);
    }

    public NamedValueSet with(String name, Object value) {
        return with(new NamedValue(name, value));
    }

    public NamedValueSet with(NamedValue... values) {
        if (values.length == 0) {
            return this;
        }
        List<NamedValue> all = new ArrayList<>(this.valuesByName.values());
        for (NamedValue f : values) {
            if (f != null) {
                all.add(f);
            }
        }
        return new NamedValueSet(all, true);
    }

    public NamedValueSet with(final Iterable<NamedValue> fields) {
        List<NamedValue> all = new ArrayList<>(this.valuesByName.values());
        fields.forEach(field -> {
            if (field != null) {
                all.add(field);
            }
        });
        return new NamedValueSet(all, true);
    }

    public Object get(final String name) {
        return this.valuesByName.get(name);
    }

    public <T> T get(final String name, final TypeConverter<T> converter) {
        return find(name).map(converter::convertValue).orElse(null);
    }

    public Optional<Object> find(final String name) {
        return Optional.ofNullable(get(name));
    }

    public <T> Optional<T> find(final String name, final TypeConverter<T> converter) {
        return Optional.ofNullable(get(name)).map(converter::convertValue);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Iterator<NamedValue> iterator() {
        return valuesByName.values().iterator();
    }

    public Map<String, Object> asMap() {
        return valuesByName.values()
                .stream()
                .collect(Collectors.toMap(NamedValue::getName, NamedValue::getValue));
    }

    public boolean isEmpty() {
        return valuesByName.isEmpty();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValueSet values = (NamedValueSet) o;
        return Objects.equals(valuesByName, values.valuesByName);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(valuesByName);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "Set" + valuesByName.values();
    }
}
