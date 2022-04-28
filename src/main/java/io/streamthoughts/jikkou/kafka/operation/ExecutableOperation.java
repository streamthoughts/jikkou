/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.operation;

import io.streamthoughts.jikkou.kafka.change.Change;
import io.vavr.concurrent.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface ExecutableOperation<T extends Change<K>, K, V> extends Operation<K, T>, Function<Collection<T>, Map<K, List<Future<V>>>> {

    /**
     * Executes this operation for the given list of changes.
     *
     * @param changes the list of change to be applied.
     * @return a map of operation results.
     */
    @Override
    default Map<K, List<Future<V>>> apply(final Collection<T> changes) {
        return this.doApply(filter(changes, this));
    }

    /**
     * Applies this operation on all changes.
     *
     * @param changes the list of change to be applied.
     * @return a map of operation results.
     */
    @NotNull Map<K, List<Future<V>>> doApply(@NotNull final Collection<T> changes);

    /**
     * Returns the {@link Change} object that match the given predicate.
     *
     * @param predicate the {@link Predicate}.
     * @return the filtered {@link Change} object.
     */
    private static <K, T extends Change<K>> Collection<T> filter(@NotNull final Collection<T> changes,
                                                                 @NotNull final Predicate<T> predicate) {
        return changes.stream().filter(predicate).collect(Collectors.toList());
    }
}

