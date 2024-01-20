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
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.streamthoughts.jikkou.common.utils.Strings;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable list of state changes.
 */
public final class ImmutableStateChangeList<T extends StateChange> implements Iterable<T>, StateChangeList<T> {

    private final List<T> changes;
    private final Map<String, List<T>> changesByName;

    /**
     * Creates a new {@link ImmutableStateChangeList} instance.
     */
    ImmutableStateChangeList() {
        this.changes = Collections.emptyList();
        this.changesByName = Collections.emptyMap();
    }

    /**
     * Creates a new {@link ImmutableStateChangeList} instance.
     *
     * @param values The values.
     */
    ImmutableStateChangeList(final List<T> values) {
        Map<String, List<T>> all = new LinkedHashMap<>();
        values.forEach(data -> {
            if (data != null) {
                all.computeIfAbsent(data.getName(), unused -> new LinkedList<>()).add(data);
            }
        });
        this.changes = Collections.unmodifiableList(values);
        this.changesByName = Collections.unmodifiableMap(all);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ImmutableStateChangeList<T> with(T... values) {
        if (values.length == 0) {
            return this;
        }
        LinkedList<T> all = new LinkedList<>(this.changes);
        for (T f : values) {
            if (f != null) {
                all.add(f);
            }
        }
        return new ImmutableStateChangeList<>(all);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ImmutableStateChangeList<T> with(final Iterable<T> values) {
        LinkedList<T> all = new LinkedList<>(this.changes);
        values.forEach(value -> {
            if (value != null) {
                all.add(value);
            }
        });
        return new ImmutableStateChangeList<>(all);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonValue
    public List<T> all() {
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public StateChangeList<T> get(final String name) {
        return new ImmutableStateChangeList<>(this.changesByName.get(name));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T getFirst(String name) {
        List<T> change = this.changesByName.getOrDefault(name, Collections.emptyList());
        if (change.isEmpty()) return null;
        return change.getFirst();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<T> findFirst(String name) {
        return Optional.ofNullable(getFirst(name));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T getLast(String name) {
        List<T> change = this.changesByName.getOrDefault(name, Collections.emptyList());
        if (change.isEmpty()) return null;
        return change.getLast();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<T> findLast(String name) {
        return Optional.ofNullable(getLast(name));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public StateChangeList<StateChange> allWithPrefix(final String prefix) {
        return allWithPrefix(prefix, true);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public StateChangeList<StateChange> allWithPrefix(final String prefix, boolean strip) {
        return new ImmutableStateChangeList<>(changes
                .stream()
                .filter(change -> change.getName().startsWith(prefix))
                .map(change -> strip ? change.withName(Strings.prunePrefix(change.getName(), prefix)) : change)
                .toList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Stream<T> stream() {
        return changes.stream();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Map<String, List<T>> asMap() {
        return changesByName;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return changes.isEmpty();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int size() {
        return changes.size();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Iterator<T> iterator() {
        return changes.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableStateChangeList<?> that = (ImmutableStateChangeList<?>) o;
        return Objects.equals(changesByName, that.changesByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "List" + changes;
    }
}