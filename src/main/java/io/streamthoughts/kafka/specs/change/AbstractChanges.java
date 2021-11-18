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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.operation.Operation;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class AbstractChanges<T extends Change<T>, K, V, O extends Operation<T, K, V>> implements Changes<T, K, V, O> {

    private final Map<K, T> changes;

    public AbstractChanges(final Map<K, T> changes) {
        this.changes = changes;
    }

    /**
     * @return the list of {@link TopicChange}.
     */
    @Override
    public List<T> all() {
        return new ArrayList<>(changes.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(@NotNull final K resource) {
        return changes.get(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OperationResult<T>> apply(@NotNull final O operation) {

        final Map<K, List<Future<V>>> results = operation.apply(changes.values());

        final List<CompletableFuture<OperationResult<T>>> futures = results.entrySet()
                .stream()
                .map(e -> new Tuple2<>(get(e.getKey()), e.getValue()))
                .map(t -> {

                    final List<Future<Option<Throwable>>> allOptions = t._2().stream()
                            .map(f -> f.map(it -> Option.<Throwable>none()))
                            .map(f -> f.recover(Option::some))
                            .collect(Collectors.toList());

                    final Future<List<Throwable>> allThrowable = Future.fold(
                            allOptions,
                            new ArrayList<>(),
                            (list, option) -> {
                                option.peek(list::add);
                                return list;
                            });

                    return allThrowable
                            .map(l -> l.isEmpty() ?
                                    OperationResult.changed(t._1(), operation.getDescriptionFor(t._1())) :
                                    OperationResult.failed(t._1(), operation.getDescriptionFor(t._1()), l))
                            .toCompletableFuture();
                })
                .collect(Collectors.toList());

        return futures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
