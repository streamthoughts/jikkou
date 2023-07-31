/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.control;

import static io.streamthoughts.jikkou.api.control.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.control.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.control.ChangeType.IGNORE;
import static io.streamthoughts.jikkou.api.control.ChangeType.NONE;
import static io.streamthoughts.jikkou.api.control.ChangeType.UPDATE;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueChangeComputer<T, V> implements ChangeComputer<T, ValueChange<V>> {

    private final ChangeKeyMapper<T> keyMapper;
    private final ChangeValueMapper<T, V> valueMapper;
    private final boolean deleteOrphans;

    /**
     * Creates a new {@link ValueChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public ValueChangeComputer(final @NotNull ChangeKeyMapper<T> keyMapper,
                               final @NotNull ChangeValueMapper<T, V> valueMapper,
                               boolean deleteOrphans) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.deleteOrphans = deleteOrphans;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ValueChange<V>> computeChanges(Iterable<T> actualStates,
                                               Iterable<T> expectedStates) {

        Map<Object, T> keyedActualStates = StreamSupport
                .stream(actualStates.spliterator(), false)
                .collect(Collectors.toMap(keyMapper::apply, it -> it));

        Map<Object, T> keyedExpectedStates = StreamSupport
                .stream(expectedStates.spliterator(), false)
                .collect(Collectors.toMap(keyMapper::apply, it -> it));

        Map<ChangeType, List<BeforeAndAfter<V>>> groupedByChangeType = new HashMap<>();

        groupedByChangeType.putAll(StreamSupport
                .stream(expectedStates.spliterator(), false)
                .collect(Collectors
                        .groupingBy(after -> {
                                    Object key = keyMapper.apply(after);
                                    T before = keyedActualStates.get(key);
                                    return getChangeType(before, after);
                                }
                                , mapping(after -> {
                                    Object key = keyMapper.apply(after);
                                    T before = keyedActualStates.get(key);
                                    return new BeforeAndAfter<>(
                                            Optional.ofNullable(before).map(it -> valueMapper.apply(it, null)).orElse(null),
                                            valueMapper.apply(before, after)
                                    );
                                }, toList())
                        )
                ));

        if (deleteOrphans) {
            List<BeforeAndAfter<V>> orphans = StreamSupport
                    .stream(actualStates.spliterator(), false)
                    .collect(Collectors
                            .groupingBy(before -> {
                                        Object key = keyMapper.apply(before);
                                        T after = keyedExpectedStates.get(key);
                                        return after == null ? DELETE : IGNORE;
                                    }
                                    , mapping(before -> {
                                        Object key = keyMapper.apply(before);
                                        T after = keyedExpectedStates.get(key);
                                        return new BeforeAndAfter<>(
                                                valueMapper.apply(before, null),
                                                Optional.ofNullable(after).map(it -> valueMapper.apply(before, after)).orElse(null)
                                        );
                                    }, toList())
                            )
                    )
                    .getOrDefault(DELETE, Collections.emptyList());
            groupedByChangeType.computeIfAbsent(DELETE, type -> new ArrayList<>()).addAll(orphans);
        }

        List<ValueChange<V>> results = new ArrayList<>();

        results.addAll(groupedByChangeType.getOrDefault(DELETE, Collections.emptyList())
                .stream().map(it -> ValueChange.withBeforeValue(it.before())).toList());

        results.addAll(groupedByChangeType.getOrDefault(ADD, Collections.emptyList())
                .stream().map(it -> ValueChange.withAfterValue(it.after())).toList());

        results.addAll(groupedByChangeType.getOrDefault(UPDATE, Collections.emptyList())
                .stream().map(it -> ValueChange.with(it.after(), it.before())).toList());

        results.addAll(groupedByChangeType.getOrDefault(NONE, Collections.emptyList())
                .stream().map(it -> ValueChange.none(it.before(), it.after())).toList());

        // IGNORE are filtered and should not be part of the result changes

        return results;
    }

    protected ChangeType getChangeType(T before, T after) {
        return before == null ? ADD : after == null ? DELETE : UPDATE;
    }

    private record BeforeAndAfter<T>(T before, T after) {
    }

    @FunctionalInterface
    public interface ChangeValueMapper<T, V> {

        /**
         * Maps a resource object into an object used for holding changes.
         *
         * @param before an actual resource object. Can be {@code null}.
         * @param after  an expected resource object. Can be {@code null}.
         * @return a grouping key
         */
        @NotNull V apply(@Nullable T before, @Nullable T after);
    }

    @FunctionalInterface
    public interface ChangeKeyMapper<T> {

        /**
         * Computes a key used to join described resources (i.e. expected state)
         * with the existing one (i.e. actual state).
         *
         * @param object a resource object.
         * @return a change key.
         */
        @NotNull Object apply(@NotNull T object);
    }
}
