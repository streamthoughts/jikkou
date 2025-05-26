/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonValue;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Reflectable
public interface StateChangeList<T extends StateChange> extends Iterable<T> {

    /**
     * Create an empty list of data changes.
     *
     * @return the data change list; never {@code null}.
     */
    static <T extends StateChange> StateChangeList<T> emptyList() {
        return new ImmutableStateChangeList<>();
    }

    /**
     * Create a list of data changes.
     *
     * @param values the named values to include
     * @return the data change list; never {@code null}.
     */
    static <T extends StateChange> StateChangeList<T> of(T... values) {
        return new ImmutableStateChangeList<T>().with(values);
    }

    /**
     * Create a list of data changes.
     *
     * @param values the values to include
     * @return the data change list; never {@code null}.
     */
    static <T extends StateChange> StateChangeList<T> of(Iterable<T> values) {
        return new ImmutableStateChangeList<T>().with(values);
    }

    /**
     * Creates a new {@link StateChangeList} with the given values.
     *
     * @param values The values.
     * @return The new {@link StateChangeList}
     */
    StateChangeList<T> with(T... values);

    /**
     * Creates a new {@link StateChangeList} with the given values.
     *
     * @param values The values.
     * @return The new {@link StateChangeList}
     */
    StateChangeList<T> with(final Iterable<T> values);

    /**
     * Gets all the data change.
     *
     * @return The data change list.
     */
    @JsonValue
    List<T> all();

    /**
     * Gets all the data change.
     *
     * @return The data change list.
     */
    default <V> List<SpecificStateChange<V>> all(TypeConverter<V> typeConverter) {
        return stream()
                .map(change -> new SpecificStateChange<>(
                        change.getName(),
                        change.getOp(),
                        typeConverter.convertValue(change.getBefore()),
                        typeConverter.convertValue(change.getAfter())
                )).toList();
    }

    /**
     * Gets all the data change for the given name.
     *
     * @param name The name of the data change.
     * @return The data change list.
     */
    StateChangeList<T> get(String name);

    /**
     * Gets the first data change for the given name.
     *
     * @param name The name of the data change.
     * @return The {@link StateChange}, or {@code null} if no change exist for the give name.
     */
    T getFirst(String name);

    /**
     * Finds the first data change for the given name.
     *
     * @param name The name of the data change.
     * @return The optional {@link StateChange}.
     */
    Optional<T> findFirst(String name);

    /**
     * Gets the last data change of this list for the given name.
     *
     * @param name The name of the data change.
     * @return The {@link StateChange}, or {@code null} if no change exist for the give name.
     */
    T getLast(String name);

    /**
     * Gets the last data change of this list for the given name.
     *
     * @param name The name of the data change.
     * @return The {@link StateChange}, or {@code null} if no change exist for the give name.
     */
    default <V> SpecificStateChange<V> getLast(String name, TypeConverter<V> typeConverter) {
        T last = getLast(name);
        if (last == null) return null;
        return new SpecificStateChange<>(
                last.getName(),
                last.getOp(),
                typeConverter.convertValue(last.getBefore()),
                typeConverter.convertValue(last.getAfter())
        );
    }

    /**
     * Finds the last data change of this list for the given name.
     *
     * @param name The name of the data change.
     * @return The optional {@link StateChange}.
     */
    Optional<T> findLast(String name);

    /**
     * Finds the last data change of this list for the given name.
     *
     * @param name The name of the data change.
     * @return The optional {@link StateChange}.
     */
    default <V> Optional<SpecificStateChange<V>> findLast(String name, TypeConverter<V> typeConverter) {
        return Optional.ofNullable(getLast(name, typeConverter));
    }

    /**
     * Gets all value changes with the given prefix.
     *
     * @param prefix The prefix.
     * @return The {@link StateChange}.
     */
    StateChangeList<StateChange> allWithPrefix(String prefix);

    /**
     * Gets all value changes with the given prefix.
     *
     * @param prefix The prefix.
     * @param strip  Specifies whether to strip the prefix before adding change to the output.
     * @return The {@link ImmutableStateChangeList}.
     */
    StateChangeList<StateChange> allWithPrefix(String prefix, boolean strip);

    /**
     * Returns a sequential Stream with this List as its source.
     *
     * @return The stream.
     */
    Stream<T> stream();

    /**
     * Gets the data changes by name.
     *
     * @return The map.
     */
    Map<String, List<T>> asMap();

    /**
     * Returns {@code true} if this List contains no elements.
     *
     * @return {@code true} if this List contains no elements
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in this List.
     *
     * @return The number of elements.
     */
    int size();
}
